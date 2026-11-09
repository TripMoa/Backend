package com.tripmoa.voucher.event;

import com.tripmoa.schedule.event.ScheduleItemDeletedEvent;
import com.tripmoa.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일정 항목이 삭제되면, 거기 연결돼 있던 바우처들의 연결만 끊는다 (바우처 자체는 삭제하지 않음)
 * - schedule 모듈이 voucher 모듈을 직접 알 필요 없도록 이벤트로 느슨하게 연결
 * - 삭제와 같은 트랜잭션 안에서 처리 (ScheduleItemService.delete()가 이미 @Transactional)
 */
@Component("voucherScheduleItemDeletedEventListener")
@RequiredArgsConstructor
public class ScheduleItemDeletedEventListener {

    private final VoucherRepository voucherRepository;

    @EventListener
    @Transactional
    public void handle(ScheduleItemDeletedEvent event) {
        voucherRepository.unlinkScheduleItem(event.scheduleItemId());
    }
}
