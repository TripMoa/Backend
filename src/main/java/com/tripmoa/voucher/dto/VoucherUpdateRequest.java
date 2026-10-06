package com.tripmoa.voucher.dto;

import com.tripmoa.voucher.enums.VoucherType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VoucherUpdateRequest(
        @NotNull
        VoucherType type,

        @NotBlank
        @Size(max = 100)
        String title,

        @Size(max = 255)
        String description,

        // 연결할 일정 항목 id (선택, null이면 연결 해제) — 같은 트립 소속이어야 함
        Long scheduleItemId
) {
}
