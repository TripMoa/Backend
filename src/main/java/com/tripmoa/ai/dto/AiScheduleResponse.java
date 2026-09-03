package com.tripmoa.ai.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AiScheduleResponse
 * - Python -> Spring 응답 DTO
 * - AI가 생성한 일정 구조를 그래도 받음
 * 구조가 반드시 Python 응답과 일치해야함
 * Python 응답 구조:
 * {
 *   "success": true,
 *   "itinerary": {
 *     "day_1": { "places": [...] },
 *     "day_2": { "places": [...] }
 *   }
 * }
 */
@Getter
public class AiScheduleResponse {

    private boolean success;
    private Itinerary itinerary;

    @Getter
    public static class Itinerary {
        private List<DayPlan> days = new ArrayList<>();

        // "day_1", "day_2" 같은 동적 키를 받아서 days 리스트로 변환
        @JsonAnySetter
        public void addDay(String key, DayPlan dayPlan) {
            if (key.startsWith("day_")) {
                int dayNum = Integer.parseInt(key.replace("day_", ""));
                dayPlan.setDay(dayNum);
                days.add(dayPlan);
            }
        }
    }

    @Getter
    public static class DayPlan {
        private int day;
        private List<Item> places;  // Python에서 "places" 필드로 옴
        private List<String> pin_warnings;        // 고정 장소 시간 충돌 경고
        private List<ExcludedPlace> excluded_places; // 용량 초과/시간대 제약으로 제외된 장소

        public void setDay(int day) {
            this.day = day;
        }
    }

    @Getter
    public static class Item {
        private String time;
        private String place;        // Python에서 "place" 필드로 옴 (title 아님)
        private String category;
        private String address;
        private String description;
        private Double lat;
        private Double lng;
        private Integer travel_minutes; //다음 장소까지 이동시간(분). ODsay 실측 값 또는 추정치
        private Integer travel_payment;  // 다음 장소까지 대중교통 요금(원). ODsay 실측값이 있을 때만 존재
        private Integer travel_transfer; // 다음 장소까지 환승 횟수. ODsay 실측값이 있을 때만 존재
    }

    @Getter
    public static class ExcludedPlace {
        private String name;
        private String category;
        private int day;
        private String reason; // "capacity" | "morning_cafe"
    }
}