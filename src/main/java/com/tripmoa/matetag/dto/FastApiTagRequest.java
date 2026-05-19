package com.tripmoa.matetag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record FastApiTagRequest(
        @JsonProperty("post_id") Long postId,
        String content,
        String destination,
        Integer budget,
        @JsonProperty("member_count") Integer memberCount,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("end_date") LocalDate endDate
    ) {}
