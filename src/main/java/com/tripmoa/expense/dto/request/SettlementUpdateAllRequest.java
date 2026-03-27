package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.PoolBalancePolicy;
import com.tripmoa.expense.enums.SplitRemainderPolicy;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SettlementUpdateAllRequest {
    private PaymentMode paymentMode;
    private SplitRemainderPolicy splitRemainderPolicy;
    private PoolBalancePolicy poolBalancePolicy;
    private Integer budgetAmount;
}
