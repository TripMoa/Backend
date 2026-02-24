package com.tripmoa.community.mate.Exception;

public class InvalidParticipantInfoException extends MateException{

    public InvalidParticipantInfoException(String message) {
        super(ErrorCode.INVALID_PARTICIPANT_INFO, message);
    }

    public InvalidParticipantInfoException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidParticipantInfoException currentTooLow() {
        return new InvalidParticipantInfoException(ErrorCode.CURRENT_PARTICIPANT_TOO_LOW);
    }

    public static InvalidParticipantInfoException maxTooLow() {
        return new InvalidParticipantInfoException(ErrorCode.MAX_PARTICIPANT_TOO_LOW);
    }

    public static InvalidParticipantInfoException currentExceedsMax() {
        return new InvalidParticipantInfoException(ErrorCode.CURRENT_EXCEEDS_MAX);
    }

    public static InvalidParticipantInfoException alreadyFullyBooked() {
        return new InvalidParticipantInfoException(ErrorCode.ALREADY_FULLY_BOOKED);
    }
}
