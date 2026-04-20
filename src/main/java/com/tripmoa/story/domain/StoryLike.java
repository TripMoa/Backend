package com.tripmoa.story.domain;

/* 여행기 좋아요 엔티티
 - 어떤 사용자가 어떤 여행기에 좋아요를 눌렀는지 기록
 - 중복 좋아요 방지를 위한 테이블 */

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.tripmoa.story.domain.Story;
import com.tripmoa.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryLike {

    // 좋아요 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 좋아요가 눌린 여행기 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // 좋아요를 누른 사용자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 좋아요 생성
    public StoryLike(Story story, User user) {
        this.story = story;
        this.user = user;
    }

}