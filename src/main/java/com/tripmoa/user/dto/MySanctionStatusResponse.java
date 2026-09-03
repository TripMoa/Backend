package com.tripmoa.user.dto;

public record MySanctionStatusResponse(
        int level,
        int totalReports,
        String status,
        boolean showWarningPopup,
        String warningMessage
) {
}
