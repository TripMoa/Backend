package com.tripmoa.user.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.style.Style;
import com.tripmoa.style.StyleRepository;
import com.tripmoa.style.UserStyle;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.user.dto.AgeVerificationResponseDto;
import com.tripmoa.user.dto.CheckEmailResponse;
import com.tripmoa.user.dto.UserResponseDto;
import com.tripmoa.user.dto.UserUpdateRequestDto;
import com.tripmoa.user.entity.SocialAccount;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.AgeVerificationStatus;
import com.tripmoa.user.enums.Gender;
import com.tripmoa.user.enums.ProfileType;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.RefreshTokenRepository;
import com.tripmoa.user.repository.SocialAccountRepository;
import com.tripmoa.user.repository.UserRepository;
import com.tripmoa.user.repository.UserSanctionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// 사용자 관련 비즈니스 로직 담당 서비스

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final StyleRepository styleRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserGuardService userGuardService;
    private final TripMemberRepository tripMemberRepository;

    // 내 정보 조회
    public UserResponseDto getMyInfo(Long userId) {
        User user = userGuardService.getActiveUserOr403(userId);
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

    public void updateUserInfo(Long userId, UserUpdateRequestDto dto) {
        User user = userGuardService.getActiveUserOr403(userId);

        // 기본 정보 업데이트
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
        }

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
                user.resetAgeVerification();
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
        }

        // 프로필 이미지
        if (dto.getProfileType() != null) {
            ProfileType targetType = ProfileType.valueOf(dto.getProfileType().toUpperCase());
            user.setProfileType(targetType);

            if (targetType == ProfileType.CUSTOM) {
                user.setProfileImage(dto.getProfileImage());
                user.setAvatarEmoji(null);
                user.setAvatarColor(null);
            } else if (targetType == ProfileType.AVATAR) {
                user.setAvatarEmoji(dto.getAvatarEmoji());
                user.setAvatarColor(dto.getAvatarColor());
                user.setProfileImage(null);
            }
        }

        // 비워진 상태에서 새로운 스타일 추가
        if (dto.getTravelStyles() != null) {
            // 기존 스타일 삭제
            user.getTravelStyles().clear();
            userRepository.saveAndFlush(user);

            for (String styleName : dto.getTravelStyles()) {
                Style style = styleRepository.findByName(styleName)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

                UserStyle userStyle = new UserStyle();
                userStyle.setUser(user);
                userStyle.setStyle(style);
                user.getTravelStyles().add(userStyle);
            }
        }
    }

    // 성인 인증
    @Transactional
    public AgeVerificationResponseDto verifyAdult(Long userId) {
        User user = userGuardService.getActiveUserOr403(userId);

        if (Boolean.TRUE.equals(user.getAgeVerified())) {
            return new AgeVerificationResponseDto(true, AgeVerificationStatus.VERIFIED.name());
        }

        userGuardService.assertBirthDateExists(userId);

        if (!user.isAdultByBirthDate()) {
            return new AgeVerificationResponseDto(
                    false,
                    AgeVerificationStatus.UNDERAGE.name()
            );
        }

        user.verifyAdult();

        return new AgeVerificationResponseDto(
                true,
                AgeVerificationStatus.VERIFIED.name()
        );
    }

    // 회원 탈퇴
    public void withdraw(Long userId) {
        User user = userGuardService.getActiveUserOr403(userId);

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
        user.setNickname("알수 없음");      // 화면 표시용
        tripMemberRepository.updateNicknameByUserId(userId, "알수 없음");
        user.setName(null);
        user.setEmail(null);              // TODO : 중복 가입 방지를 위해 필요한 경우 마스킹 처리 (예: wh***@mail.com)
        user.setNotificationEmail(null);
        user.setGender(null);
        user.setBirthDate(null);
        user.setMbti(null);
        user.setProfileImage(null);
        user.setProfileType(ProfileType.AVATAR);
        user.setAvatarEmoji("👤");
        user.setAvatarColor("#9CA3AF");
        user.setAgeVerified(false);

        userRepository.save(user);
    }

}
