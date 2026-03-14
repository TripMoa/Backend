package com.tripmoa.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* TravelStyle 태그 엔티티
 - 여행기와 연결된 태그 정보 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogTag {

    // 태그 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 태그가 연결된 여행기 ID
    private Long blogId;

    // 태그 이름
    private String tag;

    // 태그 생성
    public BlogTag(Long blogId, String tag) {
        this.blogId = blogId;
        this.tag = tag;
    }
}