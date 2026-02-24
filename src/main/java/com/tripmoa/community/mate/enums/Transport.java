package com.tripmoa.community.mate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Transport {
    BUS("버스", "bus"),
    AIRPLANE("비행기", "airplane"),
    MY_CAR("자차", "mycar"),
    RENTAL_CAR("렌트카", "rentalcar"),
    TAXI("택시", "taxi"),
    TRAIN("기차", "train"),
    WALK("도보", "walk");

    private final String korean;

    @JsonValue
    private final String value;

    @JsonCreator
    public static Transport fromValue(String input) {
        if (input == null) {
            return MY_CAR;
        }

        // 영문 값으로 찾기
        for(Transport transport : values()) {
            if(transport.value.equalsIgnoreCase(input)) {
                return transport;
            }
        }

        // 한글 값으로 찾기
        for(Transport transport : values()) {
            if(transport.korean.equals(input)) {
                return transport;
            }
        }

        throw new IllegalArgumentException(input + " is not a valid Transport");
    }
}
