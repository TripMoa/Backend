package com.tripmoa.notice.dto.response;

import com.tripmoa.notice.entity.NoticeGroup;

import java.time.LocalDateTime;

public record NoticeGroupResponse(
        Long groupId,
        Long tripId,
        String name,
        boolean isDefault,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoticeGroupResponse from(NoticeGroup noticeGroup) {
        return new NoticeGroupResponse(
                noticeGroup.getId(),
                noticeGroup.getTrip().getId(),
                noticeGroup.getName(),
                noticeGroup.isDefault(),
                noticeGroup.getSortOrder(),
                noticeGroup.getCreatedAt(),
                noticeGroup.getUpdatedAt()
        );
    }
}