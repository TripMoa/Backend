package com.tripmoa.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgeVerificationResponseDto {
    private boolean ageVerified;
    private String ageVerificationStatus;
}