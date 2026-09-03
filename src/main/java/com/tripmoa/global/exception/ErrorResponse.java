package com.tripmoa.global.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ErrorResponse
 * - 클라이언트에게 전달되는 에러 응답 DTO
 */

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        String path,
        List<FieldErrorItem> errors
) {
    public record FieldErrorItem(String field, String reason) {}
}
