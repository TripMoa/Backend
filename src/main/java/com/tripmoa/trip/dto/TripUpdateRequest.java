package com.tripmoa.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripUpdateRequest {

    @NotBlank(message = "여행 제목은 필수입니다.")
    private String title;

    @NotNull(message = "여행 시작일은 필수입니다.")
    private LocalDate tripStartDate;

    @NotNull(message = "여행 종료일은 필수입니다.")
    private LocalDate tripEndDate;

}