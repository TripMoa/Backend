package com.tripmoa.matetag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FastApiTagResponse(
        @JsonProperty("post_id") Long postId,
        @JsonProperty("style_tags") List<String> styleTags,
        @JsonProperty("vibe_tags") List<String> vibeTags
) {
}
