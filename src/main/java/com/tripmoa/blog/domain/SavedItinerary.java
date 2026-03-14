package com.tripmoa.blog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/* 일정 저장 엔티티
 - 사용자가 저장한 여행 일정 정보 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "saved_itinerary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blog_id", "user_id"}))
public class SavedItinerary {

    // 저장된 일정 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저장된 여행기 ID
    @Column(name = "blog_id", nullable = false)
    private Long blogId;

    // 저장한 사용자 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 저장된 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 일정 저장 생성
    public SavedItinerary(Long blogId, Long userId) {
        this.blogId = blogId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}