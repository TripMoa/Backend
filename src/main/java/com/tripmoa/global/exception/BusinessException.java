package com.tripmoa.global.exception;

/**
 * BusinessException
 * - 서비스 계층에서 발생하는 비즈니스 예외를 표현하는 커스텀 예외 클래스
 *   throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
 *   throw new BusinessException(ErrorCode.OCR_BAD_REQUEST, "파일이 비어있습니다.");
 */

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
