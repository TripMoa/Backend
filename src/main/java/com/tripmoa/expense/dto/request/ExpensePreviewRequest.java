package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.SplitMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ExpensePreviewRequest(

        @NotNull
        Long payerMemberId,

        @NotNull
        @Positive
        Integer totalAmount,

        @NotNull
        Boolean isShared,

        Boolean autoIncludePayer,

        @NotNull
        SplitMode splitMode,

        // EQUAL일 때 사용
        List<Long> joinedMemberIds,

        // AMOUNT일 때 사용
        @Valid
        List<ExpensePreviewManualSplitRequest> manualSplits
) {
}