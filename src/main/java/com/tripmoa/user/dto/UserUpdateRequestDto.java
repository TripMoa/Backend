package com.tripmoa.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 정보 수정 요청 DTO

@Getter
@Setter
public class UserUpdateRequestDto {
    private String nickname;
    private String name;
    private String notificationEmail;
    private String gender;
    private String birthDate;
    private String mbti;
    private List<String> travelStyles;
    private String profileImage;
    private String profileType;
    private String avatarEmoji;
    private String avatarColor;
}
