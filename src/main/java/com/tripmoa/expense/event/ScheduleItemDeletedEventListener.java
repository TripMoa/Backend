package com.tripmoa.expense.event;

import com.tripmoa.expense.repository.ExpenseRepository;
import com.tripmoa.schedule.event.ScheduleItemDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일정 항목이 삭제되면, 거기 연결돼 있던 지출들의 연결만 끊는다 (지출 자체는 삭제하지 않음)
 * - schedule 모듈이 expense 모듈을 직접 알 필요 없도록 이벤트로 느슨하게 연결
 * - 삭제와 같은 트랜잭션 안에서 처리 (ScheduleItemService.delete()가 이미 @Transactional)
 */
@Component("expenseScheduleItemDeletedEventListener")
@RequiredArgsConstructor
public class ScheduleItemDeletedEventListener {

    private final ExpenseRepository expenseRepository;

    @EventListener
    @Transactional
    public void handle(ScheduleItemDeletedEvent event) {
        expenseRepository.unlinkScheduleItem(event.scheduleItemId());
    }
}
