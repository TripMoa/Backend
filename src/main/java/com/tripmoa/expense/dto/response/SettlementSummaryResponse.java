package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.PaymentMode;
import lombok.Builder;

import java.util.List;

@Builder
public record SettlementSummaryResponse(
        Long tripId,
        PaymentMode paymentMode,
        Integer budgetAmount,
        Integer totalSpent,
        Integer remainingAmount,
        List<SummaryCategorySpendResponse> categorySpend,
        List<SummarySettlementMemberResponse> settlement,
        List<SummaryDepositStatusResponse> status
) {
}