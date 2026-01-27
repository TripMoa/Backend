package com.tripmoa.user.dto;

import com.tripmoa.user.entity.SocialAccount;
import com.tripmoa.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// 내 정보 조회 응답 DTO

@Getter
@AllArgsConstructor
public class UserResponseDto {
    public Long id;
    public String nickname;
    public String name;
    public String email;
    public String provider;
    public String notificationEmail;
    public String gender;
    public String mbti;
    public String profileImage;
    public String avatarEmoji;
    public String avatarColor;
    public LocalDate birthDate;

    // 추가: 잠금 상태 필드
    public boolean nameLocked;
    public boolean genderLocked;
    public boolean birthLocked;

    public List<String> travelStyles;

    public static UserResponseDto from(User user) {

        // 현재 연결(connected = true)된 소셜 계정 중 첫 번째를 찾아 provider 이름을 가져옴
        String providerName = user.getSocialAccounts().stream()
                .filter(sa -> Boolean.TRUE.equals(sa.getConnected()))
                .map(sa -> sa.getProvider().name())
                .findFirst()
                .orElse(null);

        return new UserResponseDto(
                user.getId(),
                user.getNickname(),
                user.getName(),
                user.getEmail(),
                providerName,
                user.getNotificationEmail(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getMbti(),
                user.getProfileImage(),
                user.getAvatarEmoji(),
                user.getAvatarColor(),
                user.getBirthDate(),

                // 잠금 필드
                user.isNameLocked(),
                user.isGenderLocked(),
                user.isBirthLocked(),

                user.getTravelStyles().stream()
                        .map(userStyle -> userStyle.getStyle().getName())
                        .toList()
        );
    }
}

