package com.tripmoa.notice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeGroupCreateRequest(

        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 60, message = "그룹명은 60자 이하여야 합니다.")
        String name,

        @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
        @Max(value = 9999, message = "정렬 순서는 9999 이하여야 합니다.")
        Integer sortOrder
) {
}