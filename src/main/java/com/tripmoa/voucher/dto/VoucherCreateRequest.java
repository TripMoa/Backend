package com.tripmoa.voucher.dto;

import com.tripmoa.voucher.enums.VoucherType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VoucherCreateRequest(
        @NotNull
        VoucherType type,

        @NotBlank
        @Size(max = 100)
        String title,

        @Size(max = 255)
        String description
) {
}
