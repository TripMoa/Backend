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

}
