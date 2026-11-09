package com.tripmoa.story.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StoryRequest {

    private String title;
    private String description;
    private String imageUrl;

    private List<String> images;
    private List<Long> tagIds;
    private String tags;

    private String destination;
    private String duration;
    private String departureDate;

    private Integer transportation;
    private Integer accommodation;
    private Integer food;
    private Integer attraction;
    private Integer shopping;

    private String type;
    private String status;

    private Boolean isPublic = true;

    // 경비 공개 여부 (기본 공개)
    private Boolean showExpense = true;

    private Long tripId;
}
