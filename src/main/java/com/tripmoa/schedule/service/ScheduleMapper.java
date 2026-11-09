package com.tripmoa.schedule.service;

import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.ai.dto.AiScheduleResponse;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.ExcludedPlaceResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* AI 응답 → DB Entity 변환
*/

public class ScheduleMapper {

    public static List<Schedule> toSchedules(Long tripId, AiScheduleResponse response, AiScheduleRequest request) {
        // AI 일정을 만들 때 사용자가 고른 값 — 이후 순서·머무는 시간을 손볼 때 시각 재계산의 기준이 된다
        String startTime = request.getDaily_start_time();
        String endTime = request.getDaily_end_time();
        String transportMode = request.getTransportation_mode();
        String lunchTime = null;
        String dinnerTime = null;
        if (request.getUser_preferences() instanceof Map<?, ?> prefs) {
            lunchTime = prefs.get("lunch_time") != null ? String.valueOf(prefs.get("lunch_time")) : null;
            dinnerTime = prefs.get("dinner_time") != null ? String.valueOf(prefs.get("dinner_time")) : null;
        }

        List<Schedule> schedules = new ArrayList<>();

        // itinerary.days 리스트 순회
        for (AiScheduleResponse.DayPlan day : response.getItinerary().getDays()) {
            schedules.add(
                    Schedule.builder()
                            .tripId(tripId)
                            .day(day.getDay())
                            .startTime(startTime)
                            .endTime(endTime)
                            .transportMode(transportMode)
                            .lunchTime(lunchTime)
                            .dinnerTime(dinnerTime)
                            .autoCompute(true)   // 새로 만든 AI 일정은 자동 계산을 켠 채로 시작
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
                            .travelMinutes(item.getTravel_minutes())
                            .travelPayment(item.getTravel_payment())
                            .travelTransfer(item.getTravel_transfer())
                            .stayMinutes(item.getStay_minutes())
                            .pinnedTime(item.getPinned_time())
                            .orderIndex(index++)
                            .build()
            );
        }
        return items;
    }

    public static List<ExcludedPlaceResponse> toExcludedPlaces(AiScheduleResponse.DayPlan dayPlan) {
        if (dayPlan.getExcluded_places() == null) return Collections.emptyList();

        List<ExcludedPlaceResponse> result = new ArrayList<>();
        for (AiScheduleResponse.ExcludedPlace e : dayPlan.getExcluded_places()) {
            result.add(
                    ExcludedPlaceResponse.builder()
                            .name(e.getName())
                            .category(e.getCategory())
                            .reason(e.getReason())
                            .build()
            );
        }
        return result;
    }

    public static List<String> toPinWarnings(AiScheduleResponse.DayPlan dayPlan) {
        return dayPlan.getPin_warnings() != null ? dayPlan.getPin_warnings() : Collections.emptyList();
    }
}