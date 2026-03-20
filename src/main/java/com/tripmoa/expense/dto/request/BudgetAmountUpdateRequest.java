package com.tripmoa.expense.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산(budgetAmount) 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class BudgetAmountUpdateRequest {

    // 예산 금액 (0 이상)
    @NotNull(message = "budgetAmount는 필수입니다.")
    @Min(value = 0, message = "budgetAmount는 0 이상이어야 합니다.")
    private Integer budgetAmount;
}