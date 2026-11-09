package com.tripmoa.schedule.dto;

import lombok.Getter;

/**
 * 구간 실시간 대중교통 경로 조회 요청 — 출발 노드와 도착 노드
 */
@Getter
public class ScheduleItemTransitRequest {
    private Long fromItemId;
    private Long toItemId;
}
