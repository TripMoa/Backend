package com.tripmoa.schedule.dto;

import lombok.Getter;

/** 그날의 자동 시각 계산을 켜거나 끈다 */
@Getter
public class ScheduleAutoComputeRequest {
    private Boolean enabled;
}
