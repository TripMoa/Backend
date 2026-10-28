package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 이 여행에서 AI 일정 생성이 진행 중인지 — 화면이 처음 열릴 때·실시간 연결이 끊겼다 붙을 때 현재 상태를 맞추는 데 쓴다
 */
@Getter
@Builder
public class ScheduleGenerationStatusResponse {

    private boolean generating;
    private Long startedAt;        // epoch millis, 진행 중이 아니면 null
    private Long actorUserId;      // 생성을 시작한 사람
    private String actorNickname;  // 그 사람의 이 여행 닉네임
}
