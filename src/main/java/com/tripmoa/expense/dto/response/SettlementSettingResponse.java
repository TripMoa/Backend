package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.PoolBalancePolicy;
import com.tripmoa.expense.enums.SplitRemainderPolicy;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 정산 설정 조회/변경 응답 DTO
 */
@Getter
@Builder
public class SettlementSettingResponse {

    // 어떤 Trip의 설정인지 식별용
    private Long tripId;

    // 정산 방식
    private PaymentMode paymentMode;

    // 지출 1/N 나머지 처리 정책 - INDEPENDENT/HYBRID 사용
    private SplitRemainderPolicy splitRemainderPolicy;

    // 통장(POOL) 남은 돈 처리 정책 - POOL/HYBRID 사용
    private PoolBalancePolicy poolBalancePolicy;

    // 예산 금액 (0 이상)
    private Integer budgetAmount;

    // 마지막 수정 시각
    private LocalDateTime updatedAt;
}