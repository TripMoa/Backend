package com.tripmoa.community.mate.Exception;

public class ParticipantFullException extends MateException {

    public ParticipantFullException(String message) {
        super(ErrorCode.PARTICIPANT_FULL, message);
    }

    public ParticipantFullException() {
        super(ErrorCode.PARTICIPANT_FULL);
    }
}
