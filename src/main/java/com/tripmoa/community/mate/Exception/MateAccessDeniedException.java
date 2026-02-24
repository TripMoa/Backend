package com.tripmoa.community.mate.Exception;

public class MateAccessDeniedException extends MateException {

    public MateAccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static MateAccessDeniedException unauthorizedPostAccess() {
        return new MateAccessDeniedException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
    }

    public static MateAccessDeniedException unauthorizedApplicationAccess() {
        return new MateAccessDeniedException(ErrorCode.UNAUTHORIZED_APPLICATION_ACCESS);
    }
}
