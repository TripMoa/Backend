package com.tripmoa.user.dto;

import com.tripmoa.user.entity.SocialAccount;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.AgeVerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class UserResponseDto {

    // ── 식별 ──────────────────────────────────────────
    private Long id;

    // ── 기본 정보 ──────────────────────────────────────
    private String nickname;
    private String name;
    private String email;
    private String notificationEmail;
    private String gender;
    private String birthDate;
    private String mbti;

    // ── 프로필 이미지 ──────────────────────────────────
    private String profileImage;
    private String profileType;
    private String avatarEmoji;
    private String avatarColor;

    // ── 소셜 계정 ──────────────────────────────────────
    private String provider;
    private List<String> linkedProviders;

    // ── 잠금 여부 ──────────────────────────────────────
    private boolean nameLocked;
    private boolean genderLocked;
    private boolean birthLocked;

    // ── 인증 ───────────────────────────────────────────
    private boolean ageVerified;
    private String ageVerificationStatus;

    // ── 여행 스타일 ────────────────────────────────────
    private List<String> travelStyles;

    public static UserResponseDto from(User user) {
        List<SocialAccount> sorted = user.getSocialAccounts().stream()
                .sorted(Comparator.comparingLong(SocialAccount::getId))
                .toList();

        String mainProvider = sorted.isEmpty()
                ? null
                : sorted.get(0).getProvider().name();

        List<String> linkedProviders = sorted.stream()
                .skip(1)
                .map(sa -> sa.getProvider().name())
                .toList();

        return UserResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .name(user.getName())
                .email(user.getEmail())
                .notificationEmail(user.getNotificationEmail())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .mbti(user.getMbti())
                .profileImage(user.getProfileImage())
                .profileType(user.getProfileType() != null ? user.getProfileType().name() : null)
                .avatarEmoji(user.getAvatarEmoji())
                .avatarColor(user.getAvatarColor())
                .provider(mainProvider)
                .linkedProviders(linkedProviders)
                .nameLocked(user.isNameLocked())
                .genderLocked(user.isGenderLocked())
                .birthLocked(user.isBirthLocked())
                .ageVerified(Boolean.TRUE.equals(user.getAgeVerified()))
                .ageVerificationStatus(user.getAgeVerificationStatus().name())
                .travelStyles(
                        user.getTravelStyles().stream()
                                .map(us -> us.getStyle().getName())
                                .toList()
                )
                .build();
    }

}