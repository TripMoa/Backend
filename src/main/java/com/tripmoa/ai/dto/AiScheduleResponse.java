//package com.tripmoa.ai.dto;
//
//import lombok.Getter;
//
//import java.util.List;
//
///**
// * AiScheduleResponse
// * - Python → Spring 응답 DTO
// * - AI가 생성한 일정 구조를 그대로 받는다
// * 구조 반드시 Python 응답과 일치해야 함
// */
//@Getter
//public class AiScheduleResponse {
//
//    //Day 단위 일정 리스트
//    private List<DayPlan> days;
//
//    //DayPlan (하루 일정)
//    @Getter
//    public static class DayPlan {
//        // 몇 번째 날인지
//        private int day;
//
//        // 해당 날짜의 일정 리스트
//        private List<Item> items;
//    }
//
//    // Item 개별 일정
//    @Getter
//    public static class Item {
//        //시간 (예: "09:00")
//        private String time;
//
//        //일정 제목 (예: "경복궁 방문")
//        private String title;
//
//        //상세 설명
//        private String description;
//    }
//
//}
package com.tripmoa.ai.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AiScheduleResponse
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
    }
}