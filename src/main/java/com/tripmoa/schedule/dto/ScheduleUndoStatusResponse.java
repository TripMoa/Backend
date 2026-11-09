package com.tripmoa.schedule.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * AI 재생성을 되돌릴 수 있는지 — 되돌릴 수 있을 때만 available 이 true 다.
 * 되돌릴 스냅샷은 있는데 그 뒤로 일정이 수정된 경우는 available=false, editedSince=true 로 이유를 알려 준다.
 */
@Getter
@Builder
public class ScheduleUndoStatusResponse {

    private boolean available;
    private boolean editedSince;       // 되돌릴 스냅샷은 있지만 재생성 뒤에 일정이 수정됨
    private String actorNickname;      // 그 재생성을 한 사람 (없으면 null)
    private Long generatedAt;          // 재생성 시각(epoch millis), 스냅샷이 없으면 null
}
