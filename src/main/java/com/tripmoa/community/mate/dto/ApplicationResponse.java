package com.tripmoa.community.mate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tripmoa.community.mate.domain.MateApplication;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.enums.ApplyStatus;
import com.tripmoa.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
public class ApplicationResponse {

    private Long id;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;

    private String postAuthorName;
    private String postAuthorEmail;
    private String postAuthorAvatar;

    private ApplyStatus status;
    private String content;
    private Long matePostId;
    private String postDestination;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String profileImage;
    private String avatar;

    private List<String> travelStyles;
    private int age;
    private String gender;

    public static ApplicationResponse from(MateApplication application) {
        User applicant = application.getApplicant();
        MatePost post = application.getMatePost();

        int age = 0;
        if (applicant.getBirthDate() != null) {
            age = java.time.Period.between(applicant.getBirthDate(), java.time.LocalDate.now()).getYears();
        }

        String genderName = (applicant.getGender() != null) ? applicant.getGender().name() : "미설정";

        return ApplicationResponse.builder()
                .id(application.getId())
                .applicantId(applicant.getId())
                .applicantName(applicant.getName())
                .applicantEmail(applicant.getEmail())
                .postAuthorName(post.getUser().getName())
                .postAuthorEmail(post.getUser().getEmail())
                .postAuthorAvatar(post.getUser().getAvatarEmoji())
                .matePostId(post.getId())
                .postDestination(post.getDestination())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .status(application.getStatus())
                .content(application.getContent())
                .profileImage(applicant.getProfileImage())
                .avatar(applicant.getAvatarEmoji())
                .travelStyles(
                        application.getApplicant().getTravelStyles().stream()
                        .map(userStyle -> userStyle.getStyle().getName())
                        .toList()
                )
                .age(age)
                .gender(genderName)
                .build();
    }

}
