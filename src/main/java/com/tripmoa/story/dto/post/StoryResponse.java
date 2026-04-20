package com.tripmoa.story.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripmoa.story.domain.Story;
import com.tripmoa.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 여행기 응답 DTO
 - 여행기 정보와 작성자 정보, 경비 정보를 프론트로 전달 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {

    // 여행기 ID
    private Long id;

    // 여행기 제목
    private String title;

    // 여행기 내용
    private String description;

    // 대표 이미지 URL
    private String imageUrl;

    // 작성자 정보
    private AuthorInfo author;

    // 좋아요 수
    private Integer likes;

    // 조회수
    private Integer views;

    // 댓글 수
    private Integer comments;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    // 여행 스타일 태그
    private String tags;

    // 여행 경비 정보
    private ExpensesInfo expenses;

    // 여행 목적지
    private String destination;

    // 여행 기간
    private String duration;

    // 출발 날짜
    private String departureDate;

    // 로그인 사용자의 좋아요 여부
    @JsonProperty("isLiked")
    private Boolean isLiked;

    /* 작성자 정보 DTO */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {

        // 작성자 ID
        private Long id;

        // 작성자 이름
        private String name;

        // 프로필 이미지 또는 아바타
        private String avatar;
    }

    /* 여행 경비 정보 DTO */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpensesInfo {

        // 교통비
        private Integer transportation;

        // 숙박비
        private Integer accommodation;

        // 식비
        private Integer food;

        // 관광/입장료
        private Integer attraction;

        // 쇼핑/기타
        private Integer shopping;

        // 총 여행 경비
        private Integer total;
    }

    // 기본 변환 (좋아요 여부 false, 댓글 수 0)
    public static StoryResponse from(Story story, User user) {
        return from(story, user, false, 0);
    }

    // 댓글 수 0 기본값
    public static StoryResponse from(Story story, User user, boolean isLiked) {
        return from(story, user, isLiked, 0);
    }

    // Story와 User를 StoryResponse로 변환
    public static StoryResponse from(Story story, User user, boolean isLiked, int commentCount) {

        AuthorInfo author = AuthorInfo.builder()
                .id(user.getId())
                .name(user.getNickname())
                .avatar(user.getProfileImage() != null ? user.getProfileImage() :
                        (user.getAvatarEmoji() != null ? user.getAvatarEmoji() :
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getId()))
                .build();

        ExpensesInfo expenses = null;

        if (story.getTransportation() != null || story.getAccommodation() != null ||
                story.getFood() != null || story.getAttraction() != null ||
                story.getShopping() != null) {

            expenses = ExpensesInfo.builder()
                    .transportation(story.getTransportation())
                    .accommodation(story.getAccommodation())
                    .food(story.getFood())
                    .attraction(story.getAttraction())
                    .shopping(story.getShopping())
                    .total(story.getTotalExpense())
                    .build();
        }

        return StoryResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .description(story.getDescription())
                .imageUrl(story.getImageUrl())
                .author(author)
                .likes(story.getLikes())
                .views(story.getViews())
                .comments(commentCount)
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .tags(story.getTags())
                .expenses(expenses)
                .destination(story.getDestination())
                .duration(story.getDuration())
                .departureDate(story.getDepartureDate())
                .isLiked(isLiked)
                .build();
    }
}