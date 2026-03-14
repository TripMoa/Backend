package com.tripmoa.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 댓글 엔티티
 - 특정 여행기(blogId)에 작성된 댓글 정보 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogComment {

    // 댓글 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글이 작성된 여행기 ID
    private Long blogId;

    // 댓글 작성자 ID
    private Long authorId;

    // 댓글 내용
    private String content;

    // 댓글 생성 시간
    private LocalDateTime createdAt;

    // 댓글 생성
    public BlogComment(Long blogId, Long authorId, String content) {
        this.blogId = blogId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // 댓글 내용 수정
    public void updateContent(String content) {
        this.content = content;
    }
}