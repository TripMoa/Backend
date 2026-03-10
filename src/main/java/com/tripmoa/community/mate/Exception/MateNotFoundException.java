package com.tripmoa.community.mate.Exception;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;

public class MateNotFoundException extends BusinessException {

    public MateNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static MateNotFoundException postNotFound() {
        return new MateNotFoundException(ErrorCode.POST_NOT_FOUND);
    }

    public static MateNotFoundException applicationNotFound() {
        return new MateNotFoundException(ErrorCode.APPLICATION_NOT_FOUND);
    }
}
