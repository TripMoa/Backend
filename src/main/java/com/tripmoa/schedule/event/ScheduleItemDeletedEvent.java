package com.tripmoa.schedule.event;

/**
 * 일정 항목(ScheduleItem)이 삭제됐을 때 발행되는 이벤트
 * - schedule 모듈이 이 이벤트를 참조하는 다른 모듈(voucher 등)을 직접 알 필요 없이
 *   느슨하게 연결하기 위한 용도
 */
public record ScheduleItemDeletedEvent(Long scheduleItemId) {
}
