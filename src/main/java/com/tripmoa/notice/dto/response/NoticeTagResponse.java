package com.tripmoa.notice.dto.response;

import com.tripmoa.notice.entity.NoticeTag;

import java.time.LocalDateTime;

public record NoticeTagResponse(
        Long tagId,
        Long tripId,
        String name,
        LocalDateTime createdAt
) {
    public static NoticeTagResponse from(NoticeTag noticeTag) {
        return new NoticeTagResponse(
                noticeTag.getId(),
                noticeTag.getTrip().getId(),
                noticeTag.getName(),
                noticeTag.getCreatedAt()
        );
    }
}