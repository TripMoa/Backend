package com.tripmoa.notice.dto.request;

import com.tripmoa.notice.enums.NoticeColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeItemCreateRequest(

        @NotNull(message = "공지 그룹 ID는 필수입니다.")
        Long noticeGroupId,

        @NotNull(message = "색상은 필수입니다.")
        NoticeColor color,

        @Size(max = 50, message = "태그는 50자 이하여야 합니다.")
        String tag,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
}