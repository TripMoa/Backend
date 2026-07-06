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
    private Integer travelMinutes; // 다음 장소까지 이동시간(분)
    private Integer travelPayment; // 다음 장소까지 대중교통 요금(원)
    private Integer travelTransfer; // 다음 장소까지 환승 횟수
}