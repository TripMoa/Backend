package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 일정 생성 시 용량 초과/시간대 제약으로 최종 일정에서 제외된 장소
 */
@Getter
@Builder
public class ExcludedPlaceResponse {
    private String name;
    private String category;
    private String reason; // "capacity" | "morning_cafe"
}
