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

    // ── 자동 시각 계산 ──
    private boolean autoCompute;        // 이 날 시각을 엔진이 자동 계산하는지
    private String startTime;           // 그날 시작 시각 (저장된 사용자 설정, 없으면 null)
    private String endTime;             // 그날 종료 시각
    private String transportMode;       // 이동수단
    private Integer overMinutes;        // 종료 시각을 넘긴 분 (넘기지 않으면 0)
    private List<ScheduleWarningResponse> warnings; // 날 단위 경고 (예: over_end)
}
