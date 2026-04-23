package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 타임라인 응답 DTO
 */
@Getter
@Builder
public class ScheduleItemResponse {
    private Long id;
    private String time;
    private String title;
    private String category;
    private String description;
    private int orderIndex;
    private Double lat;
    private Double lng;
}