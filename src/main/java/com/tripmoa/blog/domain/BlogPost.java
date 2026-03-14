package com.tripmoa.blog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/* 여행기 엔티티
 - 여행기 정보 및 경비, 조회수, 좋아요 등을 관리 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogPost {

    // 여행기 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 ID
    private Long authorId;

    // 연결된 여행 일정 ID
    private Long tripId;

    // 여행기 제목
    private String title;

    // 여행기 내용
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    // 대표 이미지 URL
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    // 여행 스타일 태그
    private String tags;

    // 여행 목적지
    private String destination;

    // 여행 기간
    private String duration;

    // 출발 날짜
    private String departureDate;

    // 경비 항목
    private Integer transportation;
    private Integer accommodation;
    private Integer food;
    private Integer attraction;
    private Integer shopping;

    // 좋아요 수
    private Integer likes = 0;

    // 조회수
    private Integer views = 0;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    // 여행기 생성
    public BlogPost(Long authorId, Long tripId, String title, String description, String imageUrl,
                    String tags, String destination, String duration, String departureDate,
                    Integer transportation, Integer accommodation,
                    Integer food, Integer attraction, Integer shopping) {
        this.authorId = authorId;
        this.tripId = tripId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.destination = destination;
        this.duration = duration;
        this.departureDate = departureDate;
        this.transportation = transportation;
        this.accommodation = accommodation;
        this.food = food;
        this.attraction = attraction;
        this.shopping = shopping;
        this.likes = 0;
        this.views = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 여행기 수정
    public void update(String title, String description, String imageUrl,
                       String tags, String destination, String duration, String departureDate,
                       Integer transportation, Integer accommodation,
                       Integer food, Integer attraction, Integer shopping) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.destination = destination;
        this.duration = duration;
        this.departureDate = departureDate;
        this.transportation = transportation;
        this.accommodation = accommodation;
        this.food = food;
        this.attraction = attraction;
        this.shopping = shopping;
        this.updatedAt = LocalDateTime.now();
    }

    // 조회수 증가
    public void incrementViews() {
        this.views++;
    }

    // 좋아요 증가
    public void incrementLikes() {
        this.likes++;
    }

    // 좋아요 감소
    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    // 총 여행 경비 계산
    public Integer getTotalExpense() {
        int total = 0;
        if (transportation != null) total += transportation;
        if (accommodation != null) total += accommodation;
        if (food != null) total += food;
        if (attraction != null) total += attraction;
        if (shopping != null) total += shopping;
        return total;
    }
}