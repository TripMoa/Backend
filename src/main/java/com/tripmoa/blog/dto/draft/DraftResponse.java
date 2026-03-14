package com.tripmoa.blog.dto.draft;

import com.tripmoa.blog.domain.Draft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 드래프트 응답 DTO
- 임시 저장된 여행기 정보를 프론트로 전달 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftResponse {

    // 드래프트 ID
    private Long id;

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

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    // Draft 엔티티를 DraftResponse로 변환
    public static DraftResponse from(Draft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .description(draft.getDescription())
                .imageUrl(draft.getImageUrl())
                .images(draft.getImages())
                .tags(draft.getTags())
                .destination(draft.getDestination())
                .duration(draft.getDuration())
                .departureDate(draft.getDepartureDate())
                .budget(draft.getBudget())
                .expenses(draft.getExpenses())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}