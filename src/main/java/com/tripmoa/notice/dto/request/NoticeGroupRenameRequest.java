package com.tripmoa.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeGroupRenameRequest(

        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 60, message = "그룹명은 60자 이하여야 합니다.")
        String name
) {
}