//package com.tripmoa.schedule.service;
//
//import com.tripmoa.ai.dto.AiScheduleResponse;
//import com.tripmoa.schedule.domain.Schedule;
//import com.tripmoa.schedule.domain.ScheduleItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * AI 응답 → DB Entity 변환
// */
//public class ScheduleMapper {
//
//    public static List<Schedule> toSchedules(Long tripId, AiScheduleResponse response){
//
//        List<Schedule> schedules = new ArrayList<>();
//
//        for(AiScheduleResponse.DayPlan day : response.getDays()) {
//            schedules.add(
//                    Schedule.builder()
//                            .tripId(tripId)
//                            .day(day.getDay())
//                            .build()
//            );
//        }
//        return schedules;
//    }
//
//    public static List<ScheduleItem> toScheduleItems(Long scheduleId,
//                                                     AiScheduleResponse.DayPlan dayPlan){
//
//        List<ScheduleItem> items = new ArrayList<>();
//
//        int index = 0;
//        for(AiScheduleResponse.Item item : dayPlan.getItems()) {
//            items.add(
//                    ScheduleItem.builder()
//                            .scheduleId(scheduleId)
//                            .time(item.getTime())
//                            .title(item.getTitle())
//                            .description(item.getDescription())
//                            .orderIndex(index++)
//                            .build()
//            );
//        }
//
//        return items;
//    }
//}
package com.tripmoa.schedule.service;

import com.tripmoa.ai.dto.AiScheduleResponse;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;

import java.util.ArrayList;
import java.util.List;

public class ScheduleMapper {

    public static List<Schedule> toSchedules(Long tripId, AiScheduleResponse response) {
        List<Schedule> schedules = new ArrayList<>();

        // itinerary.days 리스트 순회
        for (AiScheduleResponse.DayPlan day : response.getItinerary().getDays()) {
            schedules.add(
                    Schedule.builder()
                            .tripId(tripId)
                            .day(day.getDay())
                            .build()
            );
        }
        return schedules;
    }

    public static List<ScheduleItem> toScheduleItems(Long scheduleId,
                                                     AiScheduleResponse.DayPlan dayPlan) {
        List<ScheduleItem> items = new ArrayList<>();
        int index = 0;

        for (AiScheduleResponse.Item item : dayPlan.getPlaces()) {
            items.add(
                    ScheduleItem.builder()
                            .scheduleId(scheduleId)
                            .time(item.getTime() != null ? item.getTime() : "00:00")
                            .title(item.getPlace() != null ? item.getPlace() : "")
                            .category(item.getCategory() != null ? item.getCategory() : "")
                            .description(item.getAddress() != null ? item.getAddress() : "")
                            .lat(item.getLat())
                            .lng(item.getLng())
                            .orderIndex(index++)
                            .build()
            );
        }
        return items;
    }
}