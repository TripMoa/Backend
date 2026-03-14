package com.tripmoa.blog.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;

/* 여행기 생성 요청 DTO
 - 여행기 작성 시 프론트에서 전달되는 데이터 */

@Getter
@NoArgsConstructor
public class BlogCreateRequest {

    // 여행기 제목
    private String title;

    // 여행기 내용
    private String description;

    // 대표 이미지 URL
    private String imageUrl;

    // 연결된 여행 일정 ID
    private Long tripId;

    // 여행 스타일 태그
    private String tags;

    // 여행 목적지
    private String destination;

    // 여행 기간
    private String duration;

    // 출발 날짜
    private String departureDate;

    // 여행 경비 항목
    private Integer transportation;  // 교통비
    private Integer accommodation;   // 숙박비
    private Integer food;            // 식비
    private Integer attraction;      // 관광/입장료
    private Integer shopping;        // 쇼핑/기타
}