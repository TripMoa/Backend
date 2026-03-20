package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.entity.DepositLog;
import lombok.Builder;

@Builder
public record DepositLogResponse(
        Long id,
        Long tripId,
        Long memberId,
        String nickname,
        Integer amount,
        String depositDate,
        String memo,
        String depositStatus
) {
    public static DepositLogResponse from(DepositLog depositLog) {
        return DepositLogResponse.builder()
                .id(depositLog.getId())
                .tripId(depositLog.getTrip().getId())
                .memberId(depositLog.getMember().getId())
                .nickname(depositLog.getMember().getNickname())
                .amount(depositLog.getAmount())
                .depositDate(depositLog.getDepositDate().toString())
                .memo(depositLog.getMemo())
                .depositStatus(depositLog.getDepositStatus().name())
                .build();
    }
}