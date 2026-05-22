package com.tripmoa.matetag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record FastApiTagRequest(
        @JsonProperty("post_id") Long postId,
        String content,
        String destination
    ) {}
