package com.tripmoa.trip.dto;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 초대 링크 미리보기 응답.
 *
 * 초대 코드를 아는 로그인 사용자라면 멤버가 아니어도 받는 정보라서, 여행 안의 식별 정보는 최소한만 담는다.
 * - 멤버 목록은 닉네임과 아바타만 (userId·memberId·프로필 이미지는 제외)
 * - tripId 는 이미 멤버인 사람에게만 (워크스페이스로 보내기 위해). 아직 멤버가 아니면 null
 * - "이미 멤버인지"는 alreadyMember 로 따로 알려 준다 (프론트가 멤버 목록의 userId 를 뒤질 필요가 없도록)
 */
@Getter
@Builder
public class TripInviteResponse {

    private Long tripId;
    private String title;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private String ownerNickname;
    private int memberCount;
    private boolean alreadyMember;
    private List<MemberPreview> members;

    @Getter
    @Builder
    public static class MemberPreview {
        private String nickname;
        private String profileType;
        private String avatarEmoji;
        private String avatarColor;

        static MemberPreview from(TripMember member) {
            return MemberPreview.builder()
                    .nickname(member.getNickname())
                    .profileType(member.getUser() != null && member.getUser().getProfileType() != null
                            ? member.getUser().getProfileType().name()
                            : null)
                    .avatarEmoji(member.getUser() != null ? member.getUser().getAvatarEmoji() : null)
                    .avatarColor(member.getUser() != null ? member.getUser().getAvatarColor() : null)
                    .build();
        }
    }

    public static TripInviteResponse from(Trip trip, List<TripMember> members, boolean alreadyMember) {
        return TripInviteResponse.builder()
                .tripId(alreadyMember ? trip.getId() : null)
                .title(trip.getTitle())
                .tripStartDate(trip.getTripStartDate())
                .tripEndDate(trip.getTripEndDate())
                .ownerNickname(trip.getOwner().getNickname())
                .memberCount(members.size())
                .alreadyMember(alreadyMember)
                .members(members.stream()
                        .map(MemberPreview::from)
                        .toList())
                .build();
    }
}
