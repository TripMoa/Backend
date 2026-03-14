package com.tripmoa.blog.dto.tag;

/* 태그 목록 응답 DTO (TravelStyle tag)
 - 프론트에서 사용 가능한 태그 정보를 전달 */

public class TagListResponse {

    // 태그 ID
    private Long id;

    // 태그 이름
    private String name;

    public TagListResponse() {}

    public TagListResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // 태그 ID 설정
    public void setId(Long id) {
        this.id = id;
    }

    // 태그 이름 설정
    public void setName(String name) {
        this.name = name;
    }
}