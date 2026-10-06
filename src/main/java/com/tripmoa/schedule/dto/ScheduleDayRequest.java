package com.tripmoa.schedule.dto;

import lombok.Getter;

/**
 * 일차(Day) 만들기 요청 — AI 생성 전에도 직접 일정을 넣을 수 있도록 해당 일차의 Schedule 행을 보장한다
 */
@Getter
public class ScheduleDayRequest {
    private Long tripId;
    private int day;   // 1, 2, 3...
}
