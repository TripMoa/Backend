package com.tripmoa.trip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TripMemberNicknameUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
}