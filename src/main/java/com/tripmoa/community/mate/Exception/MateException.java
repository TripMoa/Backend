package com.tripmoa.community.mate.Exception;

import lombok.Getter;

@Getter
public class MateException extends RuntimeException{

    private final ErrorCode errorCode;

    public MateException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public MateException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MateException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
