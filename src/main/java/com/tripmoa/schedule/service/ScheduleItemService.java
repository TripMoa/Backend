package com.tripmoa.schedule.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.*;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleItemService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleRepository scheduleRepository;

    // 노드 추가
    @Transactional
    public ScheduleItemResponse create(ScheduleItemCreateRequest request) {

        // 1. schedule 존재 검증
        scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        //해당 day의 현재 마지막 orderIndex 계산
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
                .orderIndex(nextOrder)
                .build();

        return toResponse(scheduleItemRepository.save(item));
    }

    // 노드 수정
    @Transactional
    public ScheduleItemResponse update(Long itemId, ScheduleItemUpdateRequest request) {
        ScheduleItem item = scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        item.update(request.getTime(), request.getTitle(), request.getDescription());
        return toResponse(item);
    }

    // 노드 삭제
    @Transactional
    public void delete(Long itemId) {
        scheduleItemRepository.deleteById(itemId);
    }

    // 순서 변경 — itemIds 순서대로 orderIndex 재할당
    @Transactional
    public void reorder(ScheduleItemReorderRequest request) {

        List<Long> itemIds = request.getItemIds();

        for (int i = 0; i < itemIds.size(); i++) {
            ScheduleItem item = scheduleItemRepository.findById(itemIds.get(i))
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            item.updateOrder(i);
        }
    }

    // 다른 날로 이동
    @Transactional
    public ScheduleItemResponse move(Long itemId, ScheduleItemMoveRequest request) {
        ScheduleItem item = scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        scheduleRepository.findById(request.getTargetScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 대상 day의 마지막 orderIndex 계산
        List<ScheduleItem> targetItems = scheduleItemRepository.findAllByScheduleId(request.getTargetScheduleId());

        int nextOrder = targetItems.stream()
                .mapToInt(ScheduleItem::getOrderIndex)
                .max()
                .orElse(-1) + 1;

        item.move(request.getTargetScheduleId(), nextOrder);

        return toResponse(item);
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
                .build();
    }

}