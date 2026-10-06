package com.tripmoa.schedule.dto;

import lombok.Getter;

/**
 * 노드의 계획 값 변경
 * - stayMinutes: 머무는 시간(분, 5~720). 보내지 않으면 그대로
 * - pinnedTime: 고정 시각 "HH:MM". 빈 문자열("")이면 고정 해제, 보내지 않으면 그대로
 */
@Getter
public class ScheduleItemPlanRequest {
    private Integer stayMinutes;
    private String pinnedTime;
}
