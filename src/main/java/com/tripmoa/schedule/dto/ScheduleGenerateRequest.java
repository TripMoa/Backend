package com.tripmoa.schedule.dto;

import lombok.Getter;

import java.util.List;

/**
 * 일정 생성 요청 DTO
 * - 프론트 → 백엔드
 * - AI 서버로 넘길 데이터 포함
 */
@Getter
public class ScheduleGenerateRequest {

    private Long tripId;

    // 장소 목록 (AI에 전달)
    private List<Object> places;
    private int n_days;

    private String transportation_mode;
    private String start_date;
    private String end_date;
    private String daily_start_time;
    private String daily_end_time;

    private Object user_preferences;
    private List<Object> pinned_places;
    private List<Object> hotels;
    private List<Object> departure_points;
}
