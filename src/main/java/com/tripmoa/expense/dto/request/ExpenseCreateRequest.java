package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;
import com.tripmoa.expense.enums.SplitMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record ExpenseCreateRequest(

        @NotNull
        Long payerMemberId,

        @NotBlank
        String paidAt,

        @NotBlank
        @Size(max = 80)
        String storeName,

        @Size(max = 120)
        String itemMemo,

        @NotNull
        @Positive
        Integer totalAmount,

        @NotNull
        ExpenseCategory category,

        @NotNull
        PayMethod payMethod,

        @NotNull
        Boolean isShared,

        Boolean autoIncludePayer,

        @NotNull
        SplitMode splitMode,

        @Valid
        List<ExpenseSplitCreateRequest> splits,

        // 연결할 일정 항목 id (선택) — 같은 트립 소속이어야 함
        Long scheduleItemId

) {}