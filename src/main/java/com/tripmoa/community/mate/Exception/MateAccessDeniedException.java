package com.tripmoa.community.mate.Exception;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;

public class MateAccessDeniedException extends BusinessException {

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
