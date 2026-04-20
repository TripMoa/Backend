package com.tripmoa.story.domain;

import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.tripmoa.user.entity.User;


import java.time.LocalDateTime;

/* 댓글 엔티티
 - 특정 여행기(storyId)에 작성된 댓글 정보 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryComment {

    // 댓글 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글이 작성된 여행기 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // 댓글 작성자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    // 댓글 내용
    private String content;

    // 댓글 생성 시간
    private LocalDateTime createdAt;

    // 댓글 생성
    public StoryComment(Story story, User author, String content) {
        this.story = story;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // 댓글 내용 수정
    public void updateContent(String content) {
        this.content = content;
    }
}