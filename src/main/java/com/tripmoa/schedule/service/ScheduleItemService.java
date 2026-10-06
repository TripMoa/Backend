package com.tripmoa.schedule.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.*;
import com.tripmoa.schedule.event.ScheduleChangedEvent;
import com.tripmoa.schedule.event.ScheduleItemDeletedEvent;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleItemService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleRepository scheduleRepository;
    private final TripPermissionService tripPermissionService;
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduleAiService scheduleAiService;
    private final ScheduleComputeService scheduleComputeService;
    private final TransactionTemplate transactionTemplate;

    // 노드 추가
    // 종료 시각 초과 검사(checkAdd)가 AI 서버를 부르므로(최대 60초) 트랜잭션 밖에서 먼저 확인하고,
    // 실제 저장만 트랜잭션으로 묶는다 — 느린 외부 호출 동안 DB 커넥션을 붙잡지 않기 위함
    // (@Transactional로 통째로 감싸면 그 호출이 지연될 때 커넥션 풀이 고갈될 수 있다)
    public ScheduleItemResponse create(Long userId, ScheduleItemCreateRequest request) {
        if (request.getScheduleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "scheduleId는 필수입니다.");
        }

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        tripPermissionService.assertOwnerOrMember(schedule.getTripId(), userId);

        // 자동 계산이 켜진 날은 종료 시각을 넘기게 되는 추가를 거절
        scheduleComputeService.checkAdd(schedule, request.getTitle(), request.getCategory(), request.getLat(), request.getLng());

        return transactionTemplate.execute(status -> {
            //해당 day의 현재 마지막 orderIndex 계산 (검사 뒤 값이므로 트랜잭션 안에서 다시 조회)
            List<ScheduleItem> existing = scheduleItemRepository.findAllByScheduleId(request.getScheduleId());

            int nextOrder = existing.stream()
                    .mapToInt(ScheduleItem::getOrderIndex)
                    .max()
                    .orElse(-1) + 1;

            ScheduleItem item = ScheduleItem.builder()
                    .scheduleId(request.getScheduleId())
                    .time(request.getTime() != null ? request.getTime() : "00:00")
                    .title(request.getTitle() != null ? request.getTitle() : "NEW")
                    .description(request.getDescription() != null ? request.getDescription() : "")
                    .category(request.getCategory())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .orderIndex(nextOrder)
                    .build();

            ScheduleItemResponse created = toResponse(scheduleItemRepository.save(item));
            eventPublisher.publishEvent(new ScheduleChangedEvent(request.getScheduleId()));
            return created;
        });
    }

    // 노드 수정
    @Transactional
    public ScheduleItemResponse update(Long userId, Long itemId, ScheduleItemUpdateRequest request) {
        ScheduleItem item = getItemOr404(itemId);
        tripPermissionService.assertOwnerOrMember(getTripId(item), userId);

        item.update(request.getTime(), request.getTitle(), request.getDescription());
        return toResponse(item);
    }

    // 메모 저장 — 일정 시각과 무관하므로 재계산·초과 검사 없이 저장만 한다
    private static final int MAX_MEMO = 1000;

    @Transactional
    public ScheduleItemResponse updateMemo(Long userId, Long itemId, ScheduleItemMemoRequest request) {
        ScheduleItem item = getItemOr404(itemId);
        tripPermissionService.assertOwnerOrMember(getTripId(item), userId);

        String memo = request.getMemo() == null ? "" : request.getMemo().strip();
        if (memo.length() > MAX_MEMO) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "메모는 " + MAX_MEMO + "자까지 쓸 수 있어요.");
        }
        item.updateMemo(memo.isEmpty() ? null : memo);
        return toResponse(item);
    }

    // 노드 삭제
    @Transactional
    public void delete(Long userId, Long itemId) {
        ScheduleItem item = getItemOr404(itemId);
        tripPermissionService.assertOwnerOrMember(getTripId(item), userId);

        scheduleItemRepository.delete(item);
        eventPublisher.publishEvent(new ScheduleItemDeletedEvent(itemId));
        eventPublisher.publishEvent(new ScheduleChangedEvent(item.getScheduleId()));
    }

    // 순서 변경 — itemIds 순서를 앞에 두고, 목록에 없는 노드는 원래 상대 순서대로 뒤에 붙인 뒤
    // 그 일차 전체를 0..n-1로 다시 매긴다.
    // - 화면 목록이 낡아서(다른 멤버가 방금 노드를 추가) 일부만 보내도 순서가 겹치지 않는다.
    // - 같은 일차의 노드만 허용한다(다른 일차·다른 트립 id를 섞어 넣는 시도 방지). 중복 id는 첫 번째만 인정.
    // - checkOrder가 AI 서버를 부르므로 여기서 미리(트랜잭션 밖에서) 확인만 하고, 실제 저장은 그 뒤 새
    //   트랜잭션에서 노드를 다시 불러와 한다 — 검사에 쓴 엔티티는 그 트랜잭션이 끝나 detached라 그대로 고치면 반영되지 않는다.
    public void reorder(Long userId, ScheduleItemReorderRequest request) {

        List<Long> itemIds = request.getItemIds() == null
                ? List.of()
                : request.getItemIds().stream().distinct().toList();
        if (itemIds.isEmpty()) return;

        Long scheduleId = null;
        for (Long itemId : itemIds) {
            ScheduleItem item = getItemOr404(itemId);
            if (scheduleId == null) {
                scheduleId = item.getScheduleId();
                tripPermissionService.assertOwnerOrMember(getTripId(item), userId);
            } else if (!scheduleId.equals(item.getScheduleId())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "같은 일차의 노드만 순서를 바꿀 수 있습니다.");
            }
        }

        List<ScheduleItem> current = scheduleItemRepository.findAllByScheduleId(scheduleId).stream()
                .sorted(Comparator.comparingInt(ScheduleItem::getOrderIndex).thenComparing(ScheduleItem::getId))
                .toList();
        Map<Long, ScheduleItem> byId = current.stream()
                .collect(Collectors.toMap(ScheduleItem::getId, Function.identity()));

        List<ScheduleItem> ordered = new ArrayList<>();
        itemIds.forEach(id -> ordered.add(byId.get(id)));
        current.stream().filter(item -> !itemIds.contains(item.getId())).forEach(ordered::add);
        List<Long> orderedIds = ordered.stream().map(ScheduleItem::getId).toList();

        Schedule daySchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        scheduleComputeService.checkOrder(daySchedule, ordered);

        Long finalScheduleId = scheduleId;
        transactionTemplate.executeWithoutResult(status -> {
            Map<Long, ScheduleItem> fresh = scheduleItemRepository.findAllByScheduleId(finalScheduleId).stream()
                    .collect(Collectors.toMap(ScheduleItem::getId, Function.identity()));
            for (int i = 0; i < orderedIds.size(); i++) {
                ScheduleItem item = fresh.get(orderedIds.get(i));
                if (item != null) item.updateOrder(i);   // 검사와 저장 사이에 지워졌을 수 있어 방어적으로 건너뛴다
            }
            eventPublisher.publishEvent(new ScheduleChangedEvent(finalScheduleId));
        });
    }

    // 다른 날로 이동 (대상 day도 같은 트립 소속인지 검증)
    // checkMoveIn이 AI 서버를 부르므로 트랜잭션 밖에서 먼저 확인하고, 실제 이동만 트랜잭션으로 묶는다
    // (검사에 쓴 item은 그 조회 트랜잭션이 끝나 detached이므로 저장 트랜잭션에서 다시 불러온다)
    public ScheduleItemResponse move(Long userId, Long itemId, ScheduleItemMoveRequest request) {
        if (request.getTargetScheduleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "targetScheduleId는 필수입니다.");
        }
        ScheduleItem item = getItemOr404(itemId);
        Long tripId = getTripId(item);
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Schedule targetSchedule = scheduleRepository.findById(request.getTargetScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!targetSchedule.getTripId().equals(tripId)) {
            throw new BusinessException(ErrorCode.TRIP_FORBIDDEN);
        }

        // 자동 계산이 켜진 대상 날의 종료 시각을 넘기게 되는 이동은 거절
        scheduleComputeService.checkMoveIn(targetSchedule, item);

        Long sourceScheduleId = item.getScheduleId();
        Long targetScheduleId = request.getTargetScheduleId();
        return transactionTemplate.execute(status -> {
            ScheduleItem fresh = scheduleItemRepository.findById(itemId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

            // 대상 day의 마지막 orderIndex 계산
            List<ScheduleItem> targetItems = scheduleItemRepository.findAllByScheduleId(targetScheduleId);
            int nextOrder = targetItems.stream()
                    .mapToInt(ScheduleItem::getOrderIndex)
                    .max()
                    .orElse(-1) + 1;

            fresh.move(targetScheduleId, nextOrder);
            eventPublisher.publishEvent(new ScheduleChangedEvent(sourceScheduleId));
            eventPublisher.publishEvent(new ScheduleChangedEvent(targetScheduleId));

            return toResponse(fresh);
        });
    }

    /**
     * 두 노드 사이의 실제 대중교통 경로를 실시간으로 조회한다 (사용자가 일정 화면에서 구간을 눌렀을 때).
     * - 여행 멤버만 조회 가능, 두 노드는 같은 여행 소속이어야 한다
     * - 결과는 DB·캐시에 저장하지 않고 그대로 돌려준다 (ODsay 약관: 결과 데이터 저장 금지)
     * - DB 쓰기가 없고 조회만 하므로 트랜잭션으로 묶지 않는다 — 외부(ODsay) 호출 동안 DB 커넥션을 붙잡지 않기 위함
     */
    public java.util.Map<String, Object> transit(Long userId, ScheduleItemTransitRequest request) {
        if (request.getFromItemId() == null || request.getToItemId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "fromItemId와 toItemId는 필수입니다.");
        }

        ScheduleItem from = getItemOr404(request.getFromItemId());
        ScheduleItem to = getItemOr404(request.getToItemId());

        Long tripId = getTripId(from);
        tripPermissionService.assertOwnerOrMember(tripId, userId);
        if (!tripId.equals(getTripId(to))) {
            throw new BusinessException(ErrorCode.TRIP_FORBIDDEN);
        }

        if (from.getLat() == null || from.getLng() == null || to.getLat() == null || to.getLng() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "좌표가 없는 장소는 경로를 조회할 수 없어요.");
        }

        java.util.Map<String, Object> body = java.util.Map.of(
                "start", java.util.Map.of("lat", from.getLat(), "lng", from.getLng()),
                "end", java.util.Map.of("lat", to.getLat(), "lng", to.getLng())
        );
        return scheduleAiService.transit(body);
    }

    private ScheduleItem getItemOr404(Long itemId) {
        return scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    // ScheduleItem은 tripId를 직접 갖고 있지 않아 Schedule을 거쳐 조회
    private Long getTripId(ScheduleItem item) {
        return scheduleRepository.findById(item.getScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND))
                .getTripId();
    }

    private ScheduleItemResponse toResponse(ScheduleItem item) {
        return ScheduleItemResponse.builder()
                .id(item.getId())
                .time(item.getTime())
                .title(item.getTitle())
                .category(item.getCategory())
                .description(item.getDescription())
                .orderIndex(item.getOrderIndex())
                .lat(item.getLat())
                .lng(item.getLng())
                .memo(item.getMemo())
                .build();
    }

}
