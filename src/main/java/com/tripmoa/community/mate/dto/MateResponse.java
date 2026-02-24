package com.tripmoa.community.mate.dto;

import com.tripmoa.community.mate.enums.AgeGroup;
import com.tripmoa.community.mate.enums.GenderPreference;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.enums.Transport;
import com.tripmoa.user.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MateResponse {
    private Long id;
    private String content;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer currentParticipant;
    private Integer maxParticipant;
    private Integer budget;
    private Transport transport;
    private GenderPreference genderPreference;
    private AgeGroup ageGroup;
//    private Set<Tag> tags;
    private Long likesCount;
    private Long viewsCount;
    private LocalDateTime createdAt;
    private boolean isLiked;

    private AuthorDto author;

    public static MateResponse from(MatePost matePost) {
        return MateResponse.builder()
                .id(matePost.getId())
                .content(matePost.getContent())
                .destination(matePost.getDestination())
                .startDate(matePost.getStartDate())
                .endDate(matePost.getEndDate())
                .currentParticipant(matePost.getCurrentParticipant())
                .maxParticipant(matePost.getMaxParticipant())
                .budget(matePost.getBudget())
                .transport(matePost.getTransport())
                .genderPreference(matePost.getGenderPreference())
                .ageGroup(matePost.getAgeGroup())
                .likesCount(matePost.getLikesCount())
                .viewsCount(matePost.getViewsCount())
                .createdAt(matePost.getCreatedAt())
                .author(AuthorDto.from(matePost.getUser()))
                .build();
    }
}
