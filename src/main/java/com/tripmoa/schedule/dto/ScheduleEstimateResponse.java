package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 일정 생성 전 예상 — 현재 설정으로 생성하면 몇 곳이 들어가고 몇 곳이 빠질지
 */
@Getter
@Builder
public class ScheduleEstimateResponse {

    private int totalPlaces;                     // 보낸 방문 장소 수
    private int included;                        // 일정에 들어갈 것으로 예상되는 수
    private int excluded;                        // 빠질 것으로 예상되는 수
    private int maxPerDay;                       // 하루 최대 장소 수
    private int nDays;
    private Map<String, Integer> excludedByReason; // 사유별 개수 (capacity, meal_slot_limit, cafe_limit)
}
