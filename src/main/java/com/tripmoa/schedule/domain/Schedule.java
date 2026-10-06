package com.tripmoa.schedule.domain;

import com.tripmoa.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;

/**
 * Schedule (Day 단위 일정)
 *
 * - 하나의 여행(trip)은 여러 개의 Day를 가짐
 * - 예: Day1, Day2, Day3
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 여행에 속하는 일정인지
    private Long tripId;

    // 몇 번째 날인지 (1,2,3...)
    private int day;

    // ── 자동 시각 계산에 쓰는 그날의 설정 ─────────────────────────────────────
    // AI 일정을 만들 때 사용자가 고른 값을 저장한다. 직접 만든 일차는 비어 있고,
    // 그때는 같은 여행의 다른 일차에 저장된 사용자 설정을 가져다 쓴다(ScheduleComputeService.resolveSettings).
    private String startTime;      // "09:00"
    private String endTime;        // "21:00"
    private String transportMode;  // 택시 / 대중교통 / 도보
    private String lunchTime;      // 식사 희망 시각 ("23:59"는 식사 끄기)
    private String dinnerTime;

    // 이 날의 시각을 엔진이 자동으로 계산하는지. 새 AI 일정은 true, 기존 일정은 비어 있음(=false)
    private Boolean autoCompute;

    // 마지막 계산에서 나온 경고(JSON). 화면을 열 때마다 엔진을 부르지 않으려고 저장해 둔다
    @Column(columnDefinition = "TEXT")
    private String warningsJson;

    public boolean isAutoComputeOn() {
        return Boolean.TRUE.equals(autoCompute);
    }

    public void updateSettings(String startTime, String endTime, String transportMode) {
        if (startTime != null) this.startTime = startTime;
        if (endTime != null) this.endTime = endTime;
        if (transportMode != null) this.transportMode = transportMode;
    }

    public void setAutoCompute(boolean on) {
        this.autoCompute = on;
    }

    public void updateWarnings(String warningsJson) {
        this.warningsJson = warningsJson;
    }
}
