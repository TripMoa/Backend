package com.tripmoa.report.dto;

import java.time.LocalDateTime;

public record MyReportItemResponse(
        Long reportId,
        String location,
        Long targetId,
        String reason,
        String detail,
        String reportedNickname,
        LocalDateTime reportedAt
) {
}