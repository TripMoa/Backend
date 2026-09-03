package com.tripmoa.schedule.dto;

import lombok.Getter;

@Getter
public class ScheduleItemCreateRequest {
    private Long scheduleId;   // 어떤 day에 추가할지
    private String time;
    private String title;
    private String description;
}