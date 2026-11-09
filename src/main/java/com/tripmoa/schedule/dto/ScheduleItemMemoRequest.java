package com.tripmoa.schedule.dto;

import lombok.Getter;

/** 노드 메모 저장 — 빈 문자열이면 메모를 지운다 */
@Getter
public class ScheduleItemMemoRequest {
    private String memo;
}
