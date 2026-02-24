package com.tripmoa.community.mate.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplyStatus {
    PENDING("신청 대기", "pending"),
    APPROVED("승인됨", "approved"),
    REJECTED("거절됨", "rejected");

    private final String korean;

    @JsonValue
    private final String value;

    public static ApplyStatus fromValue(String value) {
        for(ApplyStatus status : values()) {
            if(status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("invalid status " + value);
    }

    public static ApplyStatus fromKorean(String korean) {
        for(ApplyStatus status : values()) {
            if(status.korean.equalsIgnoreCase(korean)) {
                return status;
            }
        }
        throw new IllegalArgumentException("invalid status " + korean);
    }

}

