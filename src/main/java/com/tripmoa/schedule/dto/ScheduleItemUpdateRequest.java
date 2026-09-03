package com.tripmoa.schedule.dto;

import lombok.Getter;

@Getter
public class ScheduleItemUpdateRequest {
    private String time;
    private String title;
    private String description;
}