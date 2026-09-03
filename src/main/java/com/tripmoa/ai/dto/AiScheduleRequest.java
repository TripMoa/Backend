package com.tripmoa.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AiScheduleRequest
 *
 * - Spring → Python(AI 서버) 요청 DTO
 * - 프론트에서 받은 데이터를 Python 서버 형식으로 변환해서 전달
 *
 * 주의
 * - 필드명 반드시 Python 서버와 동일해야 함
 *   (n_days 이런 스네이크 케이스 유지)
 */
@Getter
@Builder
public class AiScheduleRequest {

    //장소 리스트
    // 예:[{ "name": "경복궁", "lat": 37.5, "lng": 126.9, "category": "관광지" }]
    private List<Object> places;

    //여행 일수(Python 서버에서 일정 분배 기준으로 사용)
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
