package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScheduleResponse {

    private Long scheduleId;  // 추가: 클라이언트에서 day 식별용
    private int day;
    private List<ScheduleItemResponse> items;
    private List<String> pinWarnings;             // 고정 장소 시간 충돌 경고 (생성 시에만 채워짐)
    private List<ExcludedPlaceResponse> excludedPlaces; // 제외된 장소 (생성 시에만 채워짐)

}
