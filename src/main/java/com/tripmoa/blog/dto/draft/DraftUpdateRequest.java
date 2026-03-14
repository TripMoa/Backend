package com.tripmoa.blog.dto.draft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* 드래프트 수정 요청 DTO
 - 임시 저장된 여행기 수정 시 전달되는 데이터 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DraftUpdateRequest {

    // 여행기 제목
    private String title;

    // 여행기 내용
    private String description;

    // 대표 이미지 URL
    private String imageUrl;

    // 여행기 내부 이미지 목록
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
}