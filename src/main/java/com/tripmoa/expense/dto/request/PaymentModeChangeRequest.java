package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정산 방식(paymentMode) 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class PaymentModeChangeRequest {

    // 정산 방식 (INDEPENDENT / POOL / HYBRID)
    @NotNull(message = "paymentMode는 필수입니다.")
    private PaymentMode paymentMode;
}