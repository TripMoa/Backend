package com.tripmoa.mate.dto;

import com.tripmoa.mate.enums.AgeGroup;
import com.tripmoa.mate.enums.GenderPreference;
import com.tripmoa.mate.domain.MatePost;
import com.tripmoa.mate.enums.Transport;
import com.tripmoa.matetag.dto.MateTagResponse;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<MateTagResponse> tags;
    private Long likesCount;
    private Long viewsCount;
    private LocalDateTime createdAt;
    private boolean isLiked;
    private boolean hasApplied;

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
                .tags(matePost.getTags().stream()
                    .map(pt -> new MateTagResponse(pt.getTag().getName(), pt.getTag().getCategory()))
                    .toList())
                .likesCount(matePost.getLikesCount())
                .viewsCount(matePost.getViewsCount())
                .createdAt(matePost.getCreatedAt())
                .author(AuthorDto.from(matePost.getUser()))
                .build();
    }
}
