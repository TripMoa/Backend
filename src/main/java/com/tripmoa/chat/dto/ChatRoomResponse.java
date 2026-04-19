package com.tripmoa.chat.dto;

import com.tripmoa.chat.domain.ChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatRoomResponse {

    private String id;                  // chatRoom ID
    private String postId;              // 게시글 ID
    private String postAuthorId;        // 게시글 작성자 이메일
    private String applicantId;         // 신청자 이메일
    private String destination;         // 여행지
    private String startDate;           // 여행 시작일
    private String endDate;             // 여행 종료일
    private MemberInfo postAuthor;      // 작성자 정보
    private MemberInfo applicant;       // 신청자 정보
    private List<ChatMessageResponse> messages;
    private String lastMessageAt;
    private String createdAt;
    private int unreadCount;

    @Getter
    @Builder
    public static class MemberInfo {
        private Long id;
        private String name;
        private String email;
        private String profileImage;
        private String avatarEmoji;
        private String avatarColor;
    }

    public static ChatRoomResponse from(ChatRoom room, int unreadCount) {
        return ChatRoomResponse.builder()
                .id(String.valueOf(room.getId()))
                .postId(String.valueOf(room.getMatePost().getId()))
                .postAuthorId(room.getAuthor().getEmail())
                .applicantId(room.getApplicant().getEmail())
                .destination(room.getMatePost().getDestination())
                .startDate(room.getMatePost().getStartDate().toString())
                .endDate(room.getMatePost().getEndDate().toString())
                .postAuthor(MemberInfo.builder()
                        .id(room.getAuthor().getId())
                        .name(room.getAuthor().getName())
                        .email(room.getAuthor().getEmail())
                        .profileImage(room.getAuthor().getProfileImage())
                        .avatarEmoji(room.getAuthor().getAvatarEmoji())
                        .avatarColor(room.getAuthor().getAvatarColor())
                        .build())
                .applicant(MemberInfo.builder()
                        .id(room.getApplicant().getId())
                        .name(room.getApplicant().getName())
                        .email(room.getApplicant().getEmail())
                        .profileImage(room.getApplicant().getProfileImage())
                        .avatarEmoji(room.getAuthor().getAvatarEmoji())
                        .avatarColor(room.getAuthor().getAvatarColor())
                        .build())
                .messages(room.getMessages().stream()
                        .map(ChatMessageResponse::from)
                        .toList())
                .lastMessageAt(room.getLastMessageAt() != null
                        ? room.getLastMessageAt().toString()
                        : room.getCreatedAt().toString())
                .createdAt(room.getCreatedAt().toString())
                .unreadCount(unreadCount)
                .build();
    }
}