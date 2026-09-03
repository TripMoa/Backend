package com.tripmoa.story.dto.comment;

import com.tripmoa.story.domain.StoryComment;
import com.tripmoa.user.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/* 댓글 응답 DTO
 - 댓글 정보와 작성자 정보를 프론트로 전달 */

public class CommentResponse {

    private Long id;

    // 댓글이 작성된 여행기 ID
    private Long storyId;

    // 댓글 작성자 정보
    private AuthorInfo author;

    // 댓글 내용
    private String content;

    // 표시용 날짜
    private String date;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    /* 댓글 작성자 정보 DTO */
    public static class AuthorInfo {

        private Long id;
        private String name;
        private String nickname;
        private String avatar;
        private String profileImage;
        private String profileType;
        private String avatarEmoji;
        private String avatarColor;

        public AuthorInfo(Long id, String name, String nickname, String avatar,
                          String profileImage, String profileType,
                          String avatarEmoji, String avatarColor) {
            this.id = id;
            this.name = name;
            this.nickname = nickname;
            this.avatar = avatar;
            this.profileImage = profileImage;
            this.profileType = profileType;
            this.avatarEmoji = avatarEmoji;
            this.avatarColor = avatarColor;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getNickname() { return nickname; }
        public String getAvatar() { return avatar; }
        public String getProfileImage() { return profileImage; }
        public String getProfileType() { return profileType; }
        public String getAvatarEmoji() { return avatarEmoji; }
        public String getAvatarColor() { return avatarColor; }
    }

    public CommentResponse() {}

    public CommentResponse(Long id, Long storyId, AuthorInfo author, String content,
                           String date, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.storyId = storyId;
        this.author = author;
        this.content = content;
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // StoryComment와 User 정보를 CommentResponse로 변환
    public static CommentResponse from(StoryComment comment) {

        User user = comment.getAuthor();

        String avatar;

        // 프로필 이미지가 있으면 사용
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            avatar = user.getProfileImage();
        } else {
            // 없으면 이름 첫 글자 사용
            avatar = user.getName() != null && !user.getName().isEmpty()
                    ? user.getName().substring(0, 1).toUpperCase()
                    : "?";
        }

        AuthorInfo authorInfo = new AuthorInfo(
                user.getId(),
                user.getName(),
                user.getNickname(),
                avatar,
                user.getProfileImage(),
                user.getProfileType() != null ? user.getProfileType().name() : null,
                user.getAvatarEmoji(),
                user.getAvatarColor()
        );

        // 날짜 포맷 변환
        String formattedDate = comment.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("yyyy. M. d."));

        return new CommentResponse(
                comment.getId(),
                comment.getStory().getId(),
                authorInfo,
                comment.getContent(),
                formattedDate,
                comment.getCreatedAt(),
                comment.getCreatedAt()
        );
    }

    // 신고된 댓글 응답 - 내용과 작성자 숨김 처리
    public static CommentResponse reported(StoryComment comment) {
        AuthorInfo hiddenAuthor = new AuthorInfo(
                null, "작성자 미상", null, null, null, null, null, null
        );
        String formattedDate = comment.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("yyyy. M. d."));
        return new CommentResponse(
                comment.getId(),
                comment.getStory().getId(),
                hiddenAuthor,
                "신고된 댓글입니다.",
                formattedDate,
                comment.getCreatedAt(),
                comment.getCreatedAt()
        );
    }

    public Long getId() { return id; }
    public Long getStoryId() { return storyId; }
    public AuthorInfo getAuthor() { return author; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}