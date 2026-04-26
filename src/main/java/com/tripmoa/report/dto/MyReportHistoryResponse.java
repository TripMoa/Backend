package com.tripmoa.report.dto;

import java.util.List;

public record MyReportHistoryResponse(
        int currentLevel,
        String currentLevelLabel,
        long totalReportCount,

        int currentPage,
        int totalPages,
        long totalElements,
        int pageSize,

        List<MyReportItemResponse> reports
) {
}
