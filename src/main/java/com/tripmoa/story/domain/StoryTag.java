package com.tripmoa.story.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* TravelStyle 태그 엔티티
 - 여행기와 연결된 태그 정보 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryTag {

    // 태그 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 태그가 연결된 여행기 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // 태그 이름
    private String tag;

    // 태그 생성
    public StoryTag(Story story, String tag) {
        this.story = story;
        this.tag = tag;
    }
}