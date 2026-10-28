package com.tripmoa.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.domain.ScheduleSnapshot;
import com.tripmoa.schedule.dto.ScheduleResponse;
import com.tripmoa.schedule.dto.ScheduleUndoStatusResponse;
import com.tripmoa.schedule.event.ScheduleItemDeletedEvent;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import com.tripmoa.schedule.repository.ScheduleSnapshotRepository;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.realtime.TripChangeAction;
import com.tripmoa.trip.realtime.TripRealtimePublisher;
import com.tripmoa.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 재생성 되돌리기
 *
 * - 재생성(ScheduleService.replaceSchedules)이 일정을 바꾸기 직전에 지금 일정을 스냅샷으로 저장하고(capture),
 *   바꾼 직후의 일정 지문을 그 스냅샷에 적어 둔다(seal). 여행당 가장 최근 하나만 보관한다.
 * - 되돌리기는 "지금 일정의 지문 == 재생성 직후의 지문"일 때만 허용한다. 그 사이 누가 추가·수정·삭제·이동·메모 등
 *   무엇이든 고쳤다면 지문이 달라져서 거절한다(되돌리면 그 편집이 사라지므로). 편집 경로마다 표시를 남길 필요가 없다.
 * - 복원은 재생성과 같은 방식이다: 지금 남아 있는 노드 중 스냅샷에 있던 것은 같은 id로 되살리고(지출·바우처 연결 유지),
 *   재생성 때 지워졌던 노드는 새 id로 다시 만든다(그 노드의 지출·바우처 연결은 이미 풀려 있어서 복원되지 않는다).
 *   재생성이 새로 만든 노드는 지운다.
 */
@Service
@RequiredArgsConstructor
public class ScheduleUndoService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleSnapshotRepository snapshotRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TripScheduleLock tripLock;
    private final ScheduleGenerationState generationState;
    private final TransactionTemplate transactionTemplate;
    private final TripRealtimePublisher realtime;

    private final ObjectMapper json = new ObjectMapper();

    // ── 스냅샷에 담는 모양 ──────────────────────────────────────────────────
    public record SnapItem(Long id, String time, String title, String category, String description, int orderIndex,
                           Double lat, Double lng, Integer stayMinutes, String pinnedTime, String memo,
                           Integer travelMinutes, Integer travelPayment, Integer travelTransfer) {}

    public record SnapDay(int day, String startTime, String endTime, String transportMode, String lunchTime,
                          String dinnerTime, Boolean autoCompute, String warningsJson, List<SnapItem> items) {}

    // ── 재생성 쪽에서 부르는 부분 (재생성 트랜잭션 안) ───────────────────────────

    /**
     * 재생성이 일정을 바꾸기 직전에 지금 일정을 저장한다. 노드가 하나도 없으면 되돌릴 게 없으므로 옛 스냅샷만 지운다.
     * ※ 재생성이 기존 노드를 덮어쓰기 전에(= 노드 엔티티가 바뀌기 전에) 불러야 한다.
     */
    public void capture(Long tripId, Long userId, List<Schedule> existing) {
        List<SnapDay> days = snapshotOf(existing);
        boolean hasItems = days.stream().anyMatch(d -> !d.items().isEmpty());
        snapshotRepository.deleteByTripId(tripId);
        snapshotRepository.flush();   // 같은 트랜잭션에서 지우고 다시 넣을 때 (trip_id 유일) 순서를 보장한다
        if (!hasItems) return;

        snapshotRepository.save(new ScheduleSnapshot(tripId, write(days), userId, nicknameOf(tripId, userId)));
    }

    /** 재생성이 끝난 직후의 일정 지문을 스냅샷에 적어 둔다. 스냅샷이 없으면(되돌릴 게 없던 경우) 아무것도 안 한다. */
    public void seal(Long tripId) {
        snapshotRepository.findByTripId(tripId)
                .ifPresent(s -> s.sealWith(fingerprintOf(scheduleRepository.findAllByTripId(tripId))));
    }

    // ── 조회 ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ScheduleUndoStatusResponse status(Long tripId) {
        ScheduleSnapshot snap = snapshotRepository.findByTripId(tripId).orElse(null);
        if (snap == null || snap.getAfterFingerprint() == null) {
            return ScheduleUndoStatusResponse.builder().available(false).editedSince(false).build();
        }
        boolean unchanged = snap.getAfterFingerprint()
                .equals(fingerprintOf(scheduleRepository.findAllByTripId(tripId)));
        return ScheduleUndoStatusResponse.builder()
                .available(unchanged)
                .editedSince(!unchanged)
                .actorNickname(snap.getCreatedByNickname())
                .generatedAt(snap.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }

    // ── 되돌리기 ───────────────────────────────────────────────────────────

    public List<ScheduleResponse> undo(Long tripId) {
        generationState.assertNotGenerating(tripId);
        // 생성·일차 만들기와 같은 잠금 — 동시에 일차 행을 만들고 지우면 중복이 생긴다
        if (!tripLock.tryLock(tripId, 5, TimeUnit.SECONDS)) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "지금 이 여행의 일정을 바꾸는 중이에요. 잠시 후 다시 시도해주세요.");
        }
        try {
            List<ScheduleResponse> result = transactionTemplate.execute(status -> restore(tripId));
            // 일차 id가 모두 바뀌었다 — 받은 쪽은 전체를 다시 불러온다
            realtime.schedulesChanged(tripId, TripChangeAction.SCHEDULE_RESTORED, List.of(), null);
            return result;
        } finally {
            tripLock.unlock(tripId);
        }
    }

    private List<ScheduleResponse> restore(Long tripId) {
        ScheduleSnapshot snap = snapshotRepository.findByTripId(tripId)
                .filter(s -> s.getAfterFingerprint() != null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "되돌릴 AI 일정이 없어요."));

        List<Schedule> existing = scheduleRepository.findAllByTripId(tripId);
        if (!snap.getAfterFingerprint().equals(fingerprintOf(existing))) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "AI 일정을 만든 뒤에 일정이 수정되어 되돌릴 수 없어요.");
        }

        // 지금 남아 있는 노드 (재생성 때 재사용되었거나 새로 만들어진 것)
        Map<Long, ScheduleItem> current = new HashMap<>();
        for (Schedule schedule : existing) {
            for (ScheduleItem item : scheduleItemRepository.findAllByScheduleId(schedule.getId())) {
                current.put(item.getId(), item);
            }
        }

        List<SnapDay> days = read(snap.getPayload());
        List<Schedule> newDays = new ArrayList<>();
        for (SnapDay d : days) {
            newDays.add(Schedule.builder()
                    .tripId(tripId)
                    .day(d.day())
                    .startTime(d.startTime())
                    .endTime(d.endTime())
                    .transportMode(d.transportMode())
                    .lunchTime(d.lunchTime())
                    .dinnerTime(d.dinnerTime())
                    .autoCompute(d.autoCompute())
                    .warningsJson(d.warningsJson())
                    .build());
        }
        List<Schedule> savedDays = scheduleRepository.saveAll(newDays);

        for (int i = 0; i < days.size(); i++) {
            Long scheduleId = savedDays.get(i).getId();
            List<ScheduleItem> items = new ArrayList<>();
            for (SnapItem s : days.get(i).items()) {
                ScheduleItem fresh = ScheduleItem.builder()
                        .scheduleId(scheduleId)
                        .time(s.time()).title(s.title()).category(s.category()).description(s.description())
                        .orderIndex(s.orderIndex())
                        .lat(s.lat()).lng(s.lng())
                        .stayMinutes(s.stayMinutes()).pinnedTime(s.pinnedTime())
                        .memo(s.memo())
                        .travelMinutes(s.travelMinutes()).travelPayment(s.travelPayment()).travelTransfer(s.travelTransfer())
                        .build();
                ScheduleItem old = current.remove(s.id());
                if (old != null) {
                    old.refreshFrom(fresh);   // 같은 id 유지 — 지출·바우처 연결과 메모가 그대로 남는다
                    items.add(old);
                } else {
                    items.add(fresh);          // 재생성 때 지워졌던 노드 — 새 id
                }
            }
            scheduleItemRepository.saveAll(items);
        }

        // 재생성이 새로 만든 노드(스냅샷에 없는 것)를 지운다 — 연결된 지출·바우처는 연결만 풀린다
        List<ScheduleItem> unused = new ArrayList<>(current.values());
        unused.forEach(item -> eventPublisher.publishEvent(new ScheduleItemDeletedEvent(item.getId())));
        scheduleItemRepository.deleteAll(unused);
        scheduleRepository.deleteAll(existing);

        // 한 번 되돌린 스냅샷은 다시 쓸 수 없다
        snapshotRepository.delete(snap);

        List<ScheduleResponse> result = new ArrayList<>();
        savedDays.stream().sorted(Comparator.comparingInt(Schedule::getDay)).forEach(schedule ->
                result.add(ScheduleResponseMapper.toResponse(
                        schedule, scheduleItemRepository.findAllByScheduleId(schedule.getId()), null, null)));
        return result;
    }

    // ── 스냅샷 직렬화·지문 ──────────────────────────────────────────────────

    private List<SnapDay> snapshotOf(List<Schedule> schedules) {
        List<SnapDay> days = new ArrayList<>();
        schedules.stream()
                .sorted(Comparator.comparingInt(Schedule::getDay).thenComparing(Schedule::getId))
                .forEach(schedule -> {
                    List<SnapItem> items = scheduleItemRepository.findAllByScheduleId(schedule.getId()).stream()
                            .sorted(Comparator.comparingInt(ScheduleItem::getOrderIndex).thenComparing(ScheduleItem::getId))
                            .map(i -> new SnapItem(i.getId(), i.getTime(), i.getTitle(), i.getCategory(), i.getDescription(),
                                    i.getOrderIndex(), i.getLat(), i.getLng(), i.getStayMinutes(), i.getPinnedTime(),
                                    i.getMemo(), i.getTravelMinutes(), i.getTravelPayment(), i.getTravelTransfer()))
                            .toList();
                    days.add(new SnapDay(schedule.getDay(), schedule.getStartTime(), schedule.getEndTime(),
                            schedule.getTransportMode(), schedule.getLunchTime(), schedule.getDinnerTime(),
                            schedule.getAutoCompute(), schedule.getWarningsJson(), items));
                });
        return days;
    }

    /**
     * 일정의 지문 — 일차(번호·시간 설정·자동 계산 여부)와 노드(id·내용·순서·시각·메모 등)로 만든다.
     * 계산 경고(warningsJson)는 화면을 열 때 다시 계산되어 바뀔 수 있어서 뺐다. 일차 id도 뺐다(재생성·복원 때 매번 새로 생긴다).
     */
    private String fingerprintOf(List<Schedule> schedules) {
        StringBuilder sb = new StringBuilder();
        for (SnapDay d : snapshotOf(schedules)) {
            sb.append("D|").append(d.day()).append('|').append(d.startTime()).append('|').append(d.endTime())
                    .append('|').append(d.transportMode()).append('|').append(d.lunchTime()).append('|')
                    .append(d.dinnerTime()).append('|').append(d.autoCompute()).append('\n');
            for (SnapItem i : d.items()) {
                sb.append("I|").append(i.id()).append('|').append(i.orderIndex()).append('|').append(i.time())
                        .append('|').append(i.title()).append('|').append(i.category()).append('|')
                        .append(i.description()).append('|').append(i.lat()).append('|').append(i.lng())
                        .append('|').append(i.stayMinutes()).append('|').append(i.pinnedTime()).append('|')
                        .append(i.memo()).append('|').append(i.travelMinutes()).append('|')
                        .append(i.travelPayment()).append('|').append(i.travelTransfer()).append('\n');
            }
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String write(List<SnapDay> days) {
        try {
            return json.writeValueAsString(days);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("일정 스냅샷을 저장하지 못했어요.", e);
        }
    }

    private List<SnapDay> read(String payload) {
        try {
            return json.readValue(payload, new TypeReference<List<SnapDay>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("저장된 일정 스냅샷을 읽지 못했어요.", e);
        }
    }

    private String nicknameOf(Long tripId, Long userId) {
        if (userId == null) return null;
        return tripMemberRepository.findByTrip_IdAndUser_Id(tripId, userId)
                .map(TripMember::getNickname)
                .orElse(null);
    }
}
