package com.tripmoa.community.mate.Exception;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;

public class ParticipantFullException extends BusinessException {

    public ParticipantFullException(String message) {
        super(ErrorCode.PARTICIPANT_FULL, message);
    }

    public ParticipantFullException() {
        super(ErrorCode.PARTICIPANT_FULL);
    }
}
