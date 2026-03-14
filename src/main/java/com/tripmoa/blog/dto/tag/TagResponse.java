package com.tripmoa.blog.dto.tag;

import com.tripmoa.blog.domain.BlogTag;

/* 태그 조회 응답 DTO (TravelStyle tag)
 - 게시글에 연결된 태그 정보를 전달 */

public class TagResponse {

    // 태그 ID
    private Long id;

    // 태그가 연결된 게시글 ID
    private Long blogId;

    // 태그 이름
    private String tag;

    public TagResponse() {}

    public TagResponse(Long id, Long blogId, String tag) {
        this.id = id;
        this.blogId = blogId;
        this.tag = tag;
    }

    // BlogTag 엔티티를 TagResponse로 변환
    public static TagResponse from(BlogTag blogTag) {
        return new TagResponse(
                blogTag.getId(),
                blogTag.getBlogId(),
                blogTag.getTag()
        );
    }

    public Long getId() { return id; }
    public Long getBlogId() { return blogId; }
    public String getTag() { return tag; }
}