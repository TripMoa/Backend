package com.tripmoa.community.mate.Exception;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;

public class InvalidScheduleException extends BusinessException {

    public InvalidScheduleException(String message) {
        super(ErrorCode.INVALID_SCHEDULE, message);
    }

    public InvalidScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidScheduleException endDateBeforeStart() {
        return new InvalidScheduleException(ErrorCode.END_DATE_BEFORE_START);
    }

    public static InvalidScheduleException startDateBeforeNow() {
        return new InvalidScheduleException(ErrorCode.START_DATE_BEFORE_NOW);
    }
}
