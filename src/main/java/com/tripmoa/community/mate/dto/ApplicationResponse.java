package com.tripmoa.community.mate.dto;

import com.tripmoa.community.mate.domain.MateApplication;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.enums.ApplyStatus;
import com.tripmoa.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ApplicationResponse {

    private Long id;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private ApplyStatus status;
    private String content;
    private Long matePostId;
    private String postDestination;

    private String avatar;
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
                .matePostId(post.getId())
                .postDestination(post.getDestination())
                .status(application.getStatus())
                .content(application.getContent())
                .avatar(applicant.getAvatarEmoji())
                .age(age)
                .gender(genderName)
                .build();
    }

}
