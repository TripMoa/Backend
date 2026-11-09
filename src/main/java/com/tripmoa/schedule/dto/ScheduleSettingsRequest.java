package com.tripmoa.schedule.dto;

import lombok.Getter;

/** 그날의 시작·종료 시각과 이동수단 (보내지 않은 값은 그대로 둔다) */
@Getter
public class ScheduleSettingsRequest {
    private String startTime;      // "HH:MM"
    private String endTime;        // "HH:MM"
    private String transportMode;  // 택시 / 대중교통 / 도보
}
