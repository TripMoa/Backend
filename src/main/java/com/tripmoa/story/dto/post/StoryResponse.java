package com.tripmoa.story.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripmoa.story.domain.Story;
import com.tripmoa.story.enums.StoryType;
import com.tripmoa.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 여행기 응답 DTO
 - 여행기 정보와 작성자 정보, 경비 정보를 프론트로 전달 */

@Getter
@Builder(toBuilder = true)
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

    // 연결된 여행 ID
    private Long tripId;

    // 게시글 타입 (자유글 / 여행 후기)
    private String type;

    // 계획한 동선 스냅샷 (JSON 문자열, 없으면 null)
    private String route;

    // 로그인 사용자의 좋아요 여부
    @JsonProperty("isLiked")
    private Boolean isLiked;

    // 공개 여부
    @JsonProperty("isPublic")
    private Boolean isPublic;

    // 경비 공개 여부
    private Boolean showExpense;

    // 여행 인원 (리뷰 작성자 옆 "외 n명" 표시용, 예전 글은 null)
    private Integer memberCount;

    // 로그인 사용자가 수정·삭제할 수 있는지 (작성자 또는 그 여행 멤버). 목록 API 등 계산하지 않은 곳은 null
    private Boolean canEdit;

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

        private String profileImage;

        private String profileType;

        private String avatarEmoji;

        private String avatarColor;
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

        // 쇼핑
        private Integer shopping;

        // 기타
        private Integer etc;

        // 여행 인원 (null 이면 직접 입력하던 예전 후기)
        private Integer memberCount;

        // 총 여행 경비
        private Integer total;
    }

    // 기본 변환 (좋아요 여부 false, 댓글 수 0)
    public static StoryResponse from(Story story) {
        return from(story, false, 0);
    }

    // 댓글 수 0 기본값
    public static StoryResponse from(Story story, boolean isLiked) {
        return from(story, isLiked, 0);
    }

    public static StoryResponse from(Story story, User user, boolean isLiked, int commentCount) {
        return from(story, isLiked, commentCount);
    }

    // Story와 User를 StoryResponse로 변환
    public static StoryResponse from(Story story, boolean isLiked, int commentCount) {


        User user = story.getAuthor();

        AuthorInfo author = AuthorInfo.builder()
                .id(user.getId())
                .name(user.getNickname())
                .avatar(resolveAvatar(user))
                .profileImage(user.getProfileImage())
                .profileType(user.getProfileType() != null ? user.getProfileType().name() : null)
                .avatarEmoji(user.getAvatarEmoji())
                .avatarColor(user.getAvatarColor())
                .build();

        ExpensesInfo expenses = null;

        // 경비를 숨긴 글은 경비를 아예 보내지 않는다
        if (story.isExpenseVisible() && (
                story.getTransportation() != null || story.getAccommodation() != null ||
                story.getFood() != null || story.getAttraction() != null ||
                story.getShopping() != null || story.getEtc() != null ||
                story.getMemberCount() != null)) {

            expenses = ExpensesInfo.builder()
                    .transportation(story.getTransportation())
                    .accommodation(story.getAccommodation())
                    .food(story.getFood())
                    .attraction(story.getAttraction())
                    .shopping(story.getShopping())
                    .etc(story.getEtc())
                    .memberCount(story.getMemberCount())
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
                .tags(story.getTags())
                .expenses(expenses)
                .destination(story.getDestination())
                .duration(story.getDuration())
                .departureDate(story.getDepartureDate())
                .type(story.getType() != null ? story.getType().name() : "FREE")
                .isLiked(isLiked)
                .isPublic(story.getIsPublic())
                .showExpense(story.isExpenseVisible())
                .memberCount(story.getMemberCount())
                .tripId(story.getTrip() != null ? story.getTrip().getId() : null)
                .route(story.getRoute())
                .build();
    }

    private static String resolveAvatar(User user) {
        if (user.getProfileImage() != null) return user.getProfileImage();
        if (user.getAvatarEmoji() != null) return user.getAvatarEmoji();
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getId();
    }
}
