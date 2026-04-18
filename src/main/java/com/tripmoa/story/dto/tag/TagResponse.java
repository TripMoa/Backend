package com.tripmoa.story.dto.tag;

import com.tripmoa.story.domain.StoryTag;

/* 태그 조회 응답 DTO (TravelStyle tag)
 - 게시글에 연결된 태그 정보를 전달 */

public class TagResponse {

    // 태그 ID
    private Long id;

    // 태그가 연결된 게시글 ID
    private Long storyId;

    // 태그 이름
    private String tag;

    public TagResponse() {}

    public TagResponse(Long id, Long storyId, String tag) {
        this.id = id;
        this.storyId = storyId;
        this.tag = tag;
    }

    // StoryTag 엔티티를 TagResponse로 변환
    public static TagResponse from(StoryTag storyTag) {
        return new TagResponse(
                storyTag.getId(),
                storyTag.getStory().getId(),
                storyTag.getTag()
        );
    }

    public Long getId() { return id; }
    public Long getStoryId() { return storyId; }
    public String getTag() { return tag; }
}