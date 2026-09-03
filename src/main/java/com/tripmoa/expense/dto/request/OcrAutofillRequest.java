package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.SplitMode;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// OCR 직후 첫 preview 계산에 필요한 최소값 데이터
public record OcrAutofillRequest(
        @NotNull Long payerMemberId,
        @NotNull Boolean isShared,
        Boolean autoIncludePayer,
        @NotNull SplitMode splitMode,
        List<Long> joinedMemberIds
) {
}