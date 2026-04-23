package com.tripmoa.schedule.dto;

import lombok.Getter;

/**
 * 다른 날로 이동 요청
 */
@Getter
public class ScheduleItemMoveRequest {
    private Long targetScheduleId;  // 이동할 대상 day의 scheduleId
}