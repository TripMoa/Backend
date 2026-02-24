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

    private User applicant;
    private MatePost matePost;
    private ApplyStatus status;
    private String content;

    public static ApplicationResponse from(MateApplication application) {
        return ApplicationResponse.builder()
                .applicant(application.getApplicant())
                .matePost(application.getMatePost())
                .status(application.getStatus())
                .content(application.getContent())
                .build();
    }

}
