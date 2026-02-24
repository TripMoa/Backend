package com.tripmoa.community.mate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.enums.AgeGroup;
import com.tripmoa.community.mate.enums.GenderPreference;
import com.tripmoa.community.mate.enums.Transport;
import com.tripmoa.user.entity.User;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateRequest {

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 500, message = "내용은 500자를 초과할 수 없습니다")
    private String content;

    @NotBlank(message = "도착지는 필수입니다")
    @Size(max = 100, message = "도착지는 100자를 초과할 수 없습니다")
    private String destination;

    @NotNull(message = "출발 날짜는 필수입니다")
    @Future(message = "출발 날짜는 미래여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "도착 날짜는 필수입니다")
    @Future(message = "도착 날짜는 미래여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "현재 참여 인원은 필수입니다")
    @Min(value = 1, message = "최소 1명 이상이어야 합니다")
    private Integer currentParticipant;

    @NotNull(message = "최대 참여 인원은 필수입니다")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다")
    @Max(value = 10, message = "최대 10명까지 가능합니다")
    private Integer maxParticipant;

    @NotNull(message = "예산은 필수입니다")
    @Min(value = 0, message = "예산은 0 이상이어야 합니다")
    private Integer budget;

    @NotNull(message = "이동수단은 필수입니다")
    private Transport transport;

    private GenderPreference genderPreference;

    private AgeGroup ageGroup;

    public MatePost toEntity(User user) {
        return MatePost.builder()
                .user(user)
                .content(this.content)
                .destination(this.destination)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .currentParticipant(this.currentParticipant)
                .maxParticipant(this.maxParticipant)
                .budget(this.budget)
                .transport(this.transport)
                .genderPreference(this.genderPreference)
                .ageGroup(this.ageGroup)
                .likesCount(0L)
                .viewsCount(0L)
                .createdAt(LocalDateTime.now())
                .build();
    }
}