package com.tripmoa.notice.dto.response;

import com.tripmoa.notice.enums.NoticeColor;
import com.tripmoa.notice.entity.NoticeItem;

import java.time.LocalDateTime;

public record NoticeItemResponse(
        Long noticeItemId,
        Long noticeGroupId,
        Long createdByUserId,
        Long updatedByUserId,
        NoticeColor color,
        String tag,
        String title,
        String content,
        boolean isPinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoticeItemResponse from(NoticeItem noticeItem) {
        return new NoticeItemResponse(
                noticeItem.getId(),
                noticeItem.getNoticeGroup().getId(),
                noticeItem.getCreatedByUser().getId(),
                noticeItem.getUpdatedByUser() != null ? noticeItem.getUpdatedByUser().getId() : null,
                noticeItem.getColor(),
                noticeItem.getTag(),
                noticeItem.getTitle(),
                noticeItem.getContent(),
                noticeItem.isPinned(),
                noticeItem.getCreatedAt(),
                noticeItem.getUpdatedAt()
        );
    }
}