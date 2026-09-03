package com.tripmoa.expense.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositLogCreateRequest(

        @NotNull
        Long memberId,

        @NotNull
        @Positive
        Integer amount,

        @NotBlank
        String depositDate,

        String memo
) {
}