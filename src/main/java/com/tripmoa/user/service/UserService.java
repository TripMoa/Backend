package com.tripmoa.user.service;

import com.tripmoa.style.Style;
import com.tripmoa.style.StyleRepository;
import com.tripmoa.style.UserStyle;
import com.tripmoa.user.dto.CheckEmailResponse;
import com.tripmoa.user.dto.UserResponseDto;
import com.tripmoa.user.dto.UserUpdateRequestDto;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.Gender;
import com.tripmoa.user.enums.ProfileType;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.RefreshTokenRepository;
import com.tripmoa.user.repository.SocialAccountRepository;
import com.tripmoa.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// 사용자 관련 비즈니스 로직 담당 서비스

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final StyleRepository styleRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 내 정보 조회
    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 수동으로 생성자를 호출하는 대신 from 메서드를 사용하면 관리가 편합니다.
        return UserResponseDto.from(user);
    }

    // 가입 확인
    public CheckEmailResponse checkEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> CheckEmailResponse.builder()
                        .exists(true)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .build())
                .orElse(CheckEmailResponse.builder()
                        .exists(false)
                        .email(email)
                        .build());
    }

    // UserService 내 업데이트 로직 예시
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow();

        // 기본 정보 업데이트
        user.setNickname(dto.getNickname());
        user.setNotificationEmail(dto.getNotificationEmail());
        user.setMbti(dto.getMbti());

        // 잠금 필드 (이름, 성별, 생일)
        if (!user.isNameLocked() && dto.getName() != null) {
            user.setName(dto.getName());
            user.setNameLocked(true);
        }

        if (!user.isGenderLocked() && dto.getGender() != null) {
            user.setGender(Gender.valueOf(dto.getGender().toUpperCase()));
            user.setGenderLocked(true);
        }

        if (!user.isBirthLocked() && dto.getBirthDate() != null && !dto.getBirthDate().isBlank()) {
            try {
                user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
                user.setBirthLocked(true);
            } catch (Exception e) {
                throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. (YYYY-MM-DD)");
            }
        }

        // 프로필 이미지
        if (dto.getProfileType() != null) {
            ProfileType targetType = ProfileType.valueOf(dto.getProfileType().toUpperCase());
            user.setProfileType(targetType);

            if (targetType == ProfileType.CUSTOM) {
                if (dto.getProfileImage() != null && !dto.getProfileImage().equals(user.getProfileImage())) {
                    user.setProfileImage(dto.getProfileImage());
                }
            } else if (targetType == ProfileType.AVATAR) {
                if (dto.getAvatarEmoji() != null) user.setAvatarEmoji(dto.getAvatarEmoji());
                if (dto.getAvatarColor() != null) user.setAvatarColor(dto.getAvatarColor());
                user.setProfileImage(null);
            }
        }

        // 기존 스타일 삭제
        user.getTravelStyles().clear();
        userRepository.saveAndFlush(user);

        // 비워진 상태에서 새로운 스타일 추가
        if (dto.getTravelStyles() != null) {
            for (String styleName : dto.getTravelStyles()) {
                Style style = styleRepository.findByName(styleName)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 스타일: " + styleName));

                UserStyle userStyle = new UserStyle();
                userStyle.setUser(user);
                userStyle.setStyle(style);
                user.getTravelStyles().add(userStyle);
            }
        }
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 리프레쉬 토큰 삭제 (보안 및 세션 만료)
        refreshTokenRepository.deleteByUser(user);

        // 유저 객체와 소셜 계정 간의 메모리상 연관 관계 해제
        if (user.getSocialAccounts() != null) {
            user.getSocialAccounts().clear();
        }

        // 소셜 계정 DB 삭제
        socialAccountRepository.deleteByUser(user);

        // 유저 상태 변경 (ACTIVE -> WITHDRAWN)
        user.setStatus(UserStatus.WITHDRAWN);

        // 개인정보 익명화 (Null 처리 또는 마스킹)
        user.setNickname("알수 없음"); // 화면 표시용
        user.setName(null);
        user.setEmail(null); // 중복 가입 방지를 위해 필요한 경우 마스킹 처리 (예: wh***@mail.com)
        user.setNotificationEmail(null);
        user.setGender(null);
        user.setBirthDate(null);
        user.setMbti(null);
        user.setProfileImage(null);
        user.setAvatarEmoji(null);
        user.setAvatarColor(null);

        userRepository.save(user);
    }

}
