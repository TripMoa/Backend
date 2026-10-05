package com.tripmoa.schedule.event;

/**
 * 일차(Schedule)의 노드 구성이 바뀌었을 때(추가·삭제·순서 변경·이동) 발행되는 이벤트.
 * 커밋이 끝난 뒤 자동 계산이 켜진 날이면 시각을 다시 계산한다 (ScheduleComputeService).
 */
public record ScheduleChangedEvent(Long scheduleId) {
}
