package com.tripmoa.community.mate.dto;

import com.tripmoa.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AuthorDto {
    private Long id;
    private String nickname;
    private String email;
    private String profileImage;
    private String avatarEmoji;
    private String avatarColor;
    private String gender;
    private Integer age;
    private List<String> travelStyles;

    public static AuthorDto from(User user) {
        // 나이 계산
        Integer age = null;
        if (user.getBirthDate() != null) {
            age = LocalDate.now().getYear() - user.getBirthDate().getYear();
        }

        return AuthorDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .avatarEmoji(user.getAvatarEmoji())
                .avatarColor(user.getAvatarColor())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .age(age)
//                .travelStyles(
//                        user.getTravelStyles().stream()
//                                .map(userStyle -> userStyle.getStyle().getName())
//                                .toList()
//                )
                .build();
    }
}
