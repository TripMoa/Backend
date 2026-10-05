package com.tripmoa.schedule.service;

import com.tripmoa.ai.dto.AiEstimateResponse;
import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.ai.dto.AiScheduleResponse;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.ExcludedPlaceResponse;
import com.tripmoa.schedule.dto.ScheduleEstimateResponse;
import com.tripmoa.schedule.dto.ScheduleResponse;
import com.tripmoa.schedule.event.ScheduleItemDeletedEvent;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DB 저장 + 조회 담당
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleAiService scheduleAiService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TripScheduleLock tripLock;
    private final TransactionTemplate transactionTemplate;

    // 직접 만들 수 있는 일차의 상한 (비정상 값 방지)
    private static final int MAX_DAY = 60;

    /**
     * AI 일정 생성 후 DB 저장
     * - 기존 일정이 있으면 삭제 후 재생성 (중복 방지)
     * - 같은 여행의 생성은 한 번에 하나만: 이미 진행 중이면 409로 거절한다
     *   (동시에 실행되면 같은 일차 행이 중복으로 생긴다)
     * - AI 호출은 트랜잭션 밖에서 한다: 느린 외부 호출 동안 DB 연결·트랜잭션을 붙잡지 않고,
     *   AI가 실패해도 기존 일정은 그대로 남는다(삭제·저장은 성공한 뒤에 한 트랜잭션으로 처리)
     */
    public List<ScheduleResponse> generateAndSave(Long tripId, AiScheduleRequest request) {
        if (!tripLock.tryLock(tripId)) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "다른 멤버가 이 여행의 일정을 만들고 있어요. 잠시 후 다시 시도해주세요.");
        }
        try {
            AiScheduleResponse response = scheduleAiService.response(request);
            return transactionTemplate.execute(status -> replaceSchedules(tripId, response, request));
        } finally {
            tripLock.unlock(tripId);
        }
    }

    // AI 응답으로 기존 일정을 통째로 교체 — 반드시 트랜잭션 안에서 호출
    private List<ScheduleResponse> replaceSchedules(Long tripId, AiScheduleResponse response, AiScheduleRequest request) {

        // 재생성 케이스: 기존 일정의 노드를 "같은 장소끼리" 새 결과에 재사용한다.
        // 노드를 지우고 새로 만들면 id가 바뀌어 지출·바우처 연결과 멤버 메모가 사라지므로,
        // 새 일정에도 있는 장소(이름+좌표가 같은 것)는 그 노드를 그대로 두고 일차·순서·시각만 새 값으로 바꾼다.
        // 새 일정에 없는 장소의 노드만 지우고(연결은 이벤트로 풀림), 옛 일차(Schedule) 행은 모두 새로 만든다.
        List<Schedule> existing = scheduleRepository.findAllByTripId(tripId).stream()
                .sorted(Comparator.comparingInt(Schedule::getDay))
                .toList();
        Map<String, Deque<ScheduleItem>> reusable = new HashMap<>();
        for (Schedule old : existing) {
            scheduleItemRepository.findAllByScheduleId(old.getId()).stream()
                    .sorted(Comparator.comparingInt(ScheduleItem::getOrderIndex).thenComparing(ScheduleItem::getId))
                    .forEach(item -> reusable.computeIfAbsent(matchKey(item), k -> new ArrayDeque<>()).add(item));
        }

        // Schedule 저장 — saveAll() 반환값(ID가 채워진 객체)을 사용해야 함
        List<Schedule> schedules = ScheduleMapper.toSchedules(tripId, response, request);
        List<Schedule> savedSchedules = scheduleRepository.saveAll(schedules);

        // ScheduleItem 저장 — savedSchedules의 ID를 사용해야 scheduleId가 null이 아님
        // + 생성 시에만 존재하는 pin 경고/제외 장소를 day별로 모아둠 (DB엔 저장 안 함)
        List<AiScheduleResponse.DayPlan> dayPlans = response.getItinerary().getDays();
        Map<Integer, List<String>> pinWarningsByDay = new HashMap<>();
        Map<Integer, List<ExcludedPlaceResponse>> excludedByDay = new HashMap<>();
        for (int i = 0; i < savedSchedules.size(); i++) {
            Schedule savedSchedule = savedSchedules.get(i);
            AiScheduleResponse.DayPlan dayPlan = dayPlans.get(i);
            List<ScheduleItem> items = new ArrayList<>();
            for (ScheduleItem fresh : ScheduleMapper.toScheduleItems(savedSchedule.getId(), dayPlan)) {
                Deque<ScheduleItem> sameSpot = reusable.get(matchKey(fresh));
                ScheduleItem old = sameSpot == null ? null : sameSpot.pollFirst();
                if (old != null) {
                    old.refreshFrom(fresh);
                    items.add(old);
                } else {
                    items.add(fresh);
                }
            }
            scheduleItemRepository.saveAll(items);

            pinWarningsByDay.put(dayPlan.getDay(), ScheduleMapper.toPinWarnings(dayPlan));
            excludedByDay.put(dayPlan.getDay(), ScheduleMapper.toExcludedPlaces(dayPlan));
        }

        // 재사용되지 못한 옛 노드(새 일정에서 빠진 장소)만 삭제 — 연결된 바우처/지출은 연결만 풀어줌 (개별 삭제와 동일하게 이벤트 발행)
        List<ScheduleItem> unused = reusable.values().stream().flatMap(Collection::stream).toList();
        unused.forEach(item -> eventPublisher.publishEvent(new ScheduleItemDeletedEvent(item.getId())));
        scheduleItemRepository.deleteAll(unused);
        scheduleRepository.deleteAll(existing);

        // 저장된 일정을 바로 반환 (Controller에서 프론트로 내려줌)
        return getSchedules(tripId, pinWarningsByDay, excludedByDay);
    }

    // "같은 장소" 판정 — 이름 + 좌표(소수 넷째 자리 ≈ 10m). 좌표가 없으면 이름만 본다
    private static String matchKey(ScheduleItem item) {
        String title = item.getTitle() == null ? "" : item.getTitle().strip();
        if (item.getLat() == null || item.getLng() == null) return title + "|-|-";
        return title + "|" + Math.round(item.getLat() * 10_000) + "|" + Math.round(item.getLng() * 10_000);
    }

    /**
     * 일정 생성 전 예상 — 저장 없이 AI 서버의 예상 결과만 변환해서 돌려준다
     */
    public ScheduleEstimateResponse estimate(AiScheduleRequest request) {
        AiEstimateResponse estimate = scheduleAiService.estimate(request);

        return ScheduleEstimateResponse.builder()
                .totalPlaces(estimate.getTotal_places())
                .included(estimate.getIncluded())
                .excluded(estimate.getExcluded())
                .maxPerDay(estimate.getMax_per_day())
                .nDays(estimate.getN_days())
                .excludedByReason(estimate.getExcluded_by_reason() != null
                        ? estimate.getExcluded_by_reason()
                        : Map.of())
                .build();
    }

    /**
     * 해당 일차의 Schedule 행을 보장한다 (없으면 만들고, 있으면 그대로 돌려준다)
     * - AI 생성 전에도 직접 노드를 추가할 수 있도록, 첫 노드를 넣기 전에 프론트가 호출한다
     * - 이미 있으면 새로 만들지 않으므로 여러 번 불러도 같은 결과(멱등)
     */
    public ScheduleResponse ensureDay(Long tripId, int day) {
        if (day < 1 || day > MAX_DAY) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "일차는 1~" + MAX_DAY + " 사이여야 합니다.");
        }

        // 같은 일차를 동시에 만들면 중복 행이 생기므로 여행 단위로 직렬화한다(생성 중이면 잠깐 기다렸다가 거절)
        if (!tripLock.tryLock(tripId, 5, TimeUnit.SECONDS)) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "지금 이 여행의 일정을 만들고 있어요. 잠시 후 다시 시도해주세요.");
        }
        try {
            return transactionTemplate.execute(status -> ensureDayInTransaction(tripId, day));
        } finally {
            tripLock.unlock(tripId);
        }
    }

    private ScheduleResponse ensureDayInTransaction(Long tripId, int day) {
        Schedule schedule = scheduleRepository.findFirstByTripIdAndDayOrderByIdAsc(tripId, day)
                .orElseGet(() -> scheduleRepository.save(Schedule.builder().tripId(tripId).day(day).build()));

        return ScheduleResponseMapper.toResponse(
                schedule, scheduleItemRepository.findAllByScheduleId(schedule.getId()), null, null);
    }

    /**
     * 특정 여행의 전체 일정 조회
     * - day 오름차순, 각 day의 아이템은 orderIndex 오름차순
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long tripId) {
        return getSchedules(tripId, Map.of(), Map.of());
    }

    private List<ScheduleResponse> getSchedules(
            Long tripId,
            Map<Integer, List<String>> pinWarningsByDay,
            Map<Integer, List<ExcludedPlaceResponse>> excludedByDay
    ) {
        List<Schedule> schedules = scheduleRepository.findAllByTripId(tripId);

        return schedules.stream()
                .sorted((a, b) -> Integer.compare(a.getDay(), b.getDay()))
                .map(schedule -> ScheduleResponseMapper.toResponse(
                        schedule,
                        scheduleItemRepository.findAllByScheduleId(schedule.getId()),
                        pinWarningsByDay.getOrDefault(schedule.getDay(), Collections.emptyList()),
                        excludedByDay.getOrDefault(schedule.getDay(), Collections.emptyList())))
                .toList();
    }
}