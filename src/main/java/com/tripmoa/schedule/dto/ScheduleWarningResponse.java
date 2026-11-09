package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 자동 시각 계산에서 나온 경고
 * - itemId가 있으면 그 노드에 대한 경고, 없으면 그 날 전체에 대한 경고
 * - code: late_for_pin | meal_outside_window | after_midnight | travel_unrealistic | over_end
 */
@Getter
@Builder
public class ScheduleWarningResponse {
    private Long itemId;
    private String code;
    private String message;
    private Integer overMinutes;       // over_end일 때 넘긴 분
    private Long suggestMoveItemId;    // over_end일 때 "다른 날로 옮길까요?"의 대상 노드
}
