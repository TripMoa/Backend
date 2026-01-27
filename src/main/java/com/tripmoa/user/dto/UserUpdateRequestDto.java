package com.tripmoa.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 정보 수정 요청 DTO

@Getter
@Setter // 값 주입을 위해 필요할 수 있음
public class UserUpdateRequestDto {
    public String nickname;
    public String name;
    public String notificationEmail;
    public String gender;
    public String birthDate;
    public String mbti;
    public List<String> travelStyles;
    public String profileImage;
    public String avatarEmoji;
    public String avatarColor;
}
