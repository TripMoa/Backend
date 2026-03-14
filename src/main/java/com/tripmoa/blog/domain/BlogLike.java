package com.tripmoa.blog.domain;

/* 여행기 좋아요 엔티티
 - 어떤 사용자가 어떤 여행기에 좋아요를 눌렀는지 기록
 - 중복 좋아요 방지를 위한 테이블 */

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogLike {

    // 좋아요 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 좋아요가 눌린 여행기 ID
    private Long blogId;

    // 좋아요를 누른 사용자 ID
    private Long userId;

    // 좋아요 생성
    public BlogLike(Long blogId, Long userId) {
        this.blogId = blogId;
        this.userId = userId;
    }

}