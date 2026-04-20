package com.tripmoa.story.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.tripmoa.user.entity.User;

/* 일정 저장 엔티티
 - 사용자가 저장한 여행 일정 정보 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "saved_itinerary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"story_id", "user_id"}))
public class SavedItinerary {

    // 저장된 일정 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저장된 여행기 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // 저장한 사용자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 저장된 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 일정 저장 생성
    public SavedItinerary(Story story, User user) {
        this.story = story;
        this.user = user;
    }

    // 엔티티 저장 시 생성 시간을 자동으로 설정
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}