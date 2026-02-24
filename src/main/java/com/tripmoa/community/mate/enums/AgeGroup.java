package com.tripmoa.community.mate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    ALL("전체", "all"),
    TWENTIES("20대", "20s"),
    THIRTIES("30대", "30s"),
    FORTIES("40대", "40s"),
    FIFTIES_PLUS("50대 이상", "50s+");

    private final String korean;

    @JsonValue
    private final String value;

    @JsonCreator
    public static AgeGroup fromValue(String input) {
        if (input == null) {
            return ALL;
        }

        for(AgeGroup ageGroup : values()) {
            if(ageGroup.value.equalsIgnoreCase(input)) {
                return ageGroup;
            }
        }

        for(AgeGroup ageGroup : values()) {
            if(ageGroup.korean.equals(input)) {
                return ageGroup;
            }
        }

        throw new IllegalArgumentException(input + " is not a valid AgeGroup");
    }

    public static AgeGroup fromKorean(String korean) {
        for(AgeGroup ageGroup : values()) {
            if(ageGroup.korean.equals(korean)) {
                return ageGroup;
            }
        }
        throw new IllegalArgumentException(korean + " is not a valid AgeGroup");
    }
}