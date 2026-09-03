package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.PaymentMode;
import lombok.Builder;

import java.util.List;

@Builder
public record SettlementSummaryResponse(
        Long tripId,
        PaymentMode paymentMode,
        Integer budgetAmount,
        int totalSpent,                                                 // 공동 지출 합계
        int personalTotal,                                              // 개인 지출 합계
        Integer remainingAmount,
        List<SummaryCategorySpendResponse> categorySpend,
        List<SummarySettlementMemberResponse> settlement,
        List<SummaryDepositStatusResponse> status,
        List<SettlementTransactionResponse> transactions
) {
}