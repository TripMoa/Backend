package com.tripmoa.trip.dto;

import com.tripmoa.trip.entity.TripMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripMemberResponse {
    private Long memberId;
    private Long userId;
    private String nickname;
    private int sortOrder;

    private String profileImage;
    private String profileType;
    private String avatarEmoji;
    private String avatarColor;

    public static TripMemberResponse from(TripMember member) {
        return TripMemberResponse.builder()
                .memberId(member.getId())
                .userId(member.getUser() != null ? member.getUser().getId() : null)
                .nickname(member.getNickname())
                .sortOrder(member.getSortOrder())
                .profileImage(member.getUser() != null ? member.getUser().getProfileImage() : null)
                .profileType(member.getUser() != null && member.getUser().getProfileType() != null
                        ? member.getUser().getProfileType().name()
                        : null)
                .avatarEmoji(member.getUser() != null ? member.getUser().getAvatarEmoji() : null)
                .avatarColor(member.getUser() != null ? member.getUser().getAvatarColor() : null)
                .build();
    }
}