package com.tripmoa.blog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/* 임시 저장(드래프트) 엔티티
 - 작성 중인 여행기 초안 데이터를 저장 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Draft {

    // 드래프트 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 ID
    private Long authorId;

    // 여행기 제목
    private String title;

    // 여행기 내용
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    // 대표 이미지 URL
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    // 여행기 내부 이미지 목록
    @Column(columnDefinition = "LONGTEXT")
    private String images;

    // 여행 스타일 태그
    private String tags;

    // 여행 목적지
    private String destination;

    // 여행 기간
    private String duration;

    // 출발 날짜
    private String departureDate;

    // 총 예산
    private String budget;

    // 상세 경비 정보
    private String expenses;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    // 드래프트 생성
    public Draft(Long authorId, String title, String description, String imageUrl,
                 String images, String tags, String destination, String duration,
                 String departureDate, String budget, String expenses) {
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.images = images;
        this.tags = tags;
        this.destination = destination;
        this.duration = duration;
        this.departureDate = departureDate;
        this.budget = budget;
        this.expenses = expenses;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 드래프트 수정
    public void update(String title, String description, String imageUrl,
                       String images, String tags, String destination, String duration,
                       String departureDate, String budget, String expenses) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.images = images;
        this.tags = tags;
        this.destination = destination;
        this.duration = duration;
        this.departureDate = departureDate;
        this.budget = budget;
        this.expenses = expenses;
        this.updatedAt = LocalDateTime.now();
    }
}