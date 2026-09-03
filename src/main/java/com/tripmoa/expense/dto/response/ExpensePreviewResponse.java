package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.SplitMode;
import lombok.Builder;

import java.util.List;

@Builder
public record ExpensePreviewResponse(
        PaymentMode paymentMode,
        SplitMode splitMode,
        boolean isShared,
        int totalAmount,
        int splitTotalAmount,
        int diffAmount,
        boolean canSave,
        String message,
        List<ExpensePreviewSplitResponse> splits
) {
}