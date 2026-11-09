package com.tripmoa.ai.dto;

import lombok.Getter;

import java.util.Map;

/**
 * AiEstimateResponse
 * - Python /schedule/estimate 응답 DTO (일정 생성 전 예상)
 * 구조가 Python 응답과 일치해야 한다.
 * {
 *   "success": true,
 *   "total_places": 12, "included": 8, "excluded": 4,
 *   "max_per_day": 4, "n_days": 2,
 *   "excluded_by_reason": { "capacity": 4 }
 * }
 */
@Getter
public class AiEstimateResponse {

    private boolean success;
    private int total_places;
    private int included;
    private int excluded;
    private int max_per_day;
    private int n_days;
    private Map<String, Integer> excluded_by_reason;
}
