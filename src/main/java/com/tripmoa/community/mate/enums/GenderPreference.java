package com.tripmoa.community.mate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderPreference {
    ANY("무관", "any"),
    MALE("남성", "male"),
    FEMALE("여성", "female");

    private final String korean;

    @JsonValue
    private final String value;

    @JsonCreator
    public static GenderPreference fromValue(String input) {
        if (input == null) {
            return ANY;
        }

        // 영문 값으로 찾기
        for(GenderPreference gender : values()) {
            if(gender.value.equalsIgnoreCase(input)) {
                return gender;
            }
        }

        // 한글 값으로 찾기
        for(GenderPreference gender : values()) {
            if(gender.korean.equals(input)) {
                return gender;
            }
        }

        throw new IllegalArgumentException(input + " is not a valid GenderPreference");
    }
}