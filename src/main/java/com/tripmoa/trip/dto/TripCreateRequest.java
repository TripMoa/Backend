package com.tripmoa.trip.dto;

import com.tripmoa.trip.enums.TripVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class TripCreateRequest {

    @NotBlank(message = "여행 제목은 필수입니다.")
    private String title;

    @NotNull(message = "여행 시작일은 필수입니다.")
    private LocalDate tripStartDate;

    @NotNull(message = "여행 종료일은 필수입니다.")
    private LocalDate tripEndDate;

    // 초대 멤버 userId 목록 (없으면 null 또는 빈 리스트 허용)
    private List<Long> memberUserIds;
}