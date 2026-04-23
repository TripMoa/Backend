package com.tripmoa.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequest {

    @NotNull(message = "신고 대상 유저 ID는 필수입니다")
    private Long reportedUserId;

    @NotNull(message = "신고 위치는 필수입니다")
    private ReportLocation location;

    @NotNull(message = "대상 ID는 필수입니다")
    private Long targetId;

    @NotBlank(message = "신고 사유는 필수입니다")
    private String reason;

    private String detail;
    private String contentSnapshot;
    private String reportedNickname;
}