package com.tripmoa.user.service;

import com.tripmoa.style.Style;
import com.tripmoa.style.StyleRepository;
import com.tripmoa.style.UserStyle;
import com.tripmoa.user.dto.UserResponseDto;
import com.tripmoa.user.dto.UserUpdateRequestDto;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.Gender;
import com.tripmoa.user.enums.UserStatus;
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

    // 내 정보 조회
    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 수동으로 생성자를 호출하는 대신 from 메서드를 사용하면 관리가 편합니다.
        return UserResponseDto.from(user);
    }

    // UserService 내 업데이트 로직 예시
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow();

        // 닉네임은 언제든 수정 가능 (필수)
        user.setNickname(dto.getNickname());

        user.setNotificationEmail(dto.getNotificationEmail());

        // 이름: 잠겨있지 않을 때만 수정하고, 수정 시 잠금 처리
        if (!user.isNameLocked() && dto.getName() != null) {
            user.setName(dto.getName());
            user.setNameLocked(true);
        }

        // 성별: 잠겨있지 않을 때만 수정하고 잠금
        if (!user.isGenderLocked() && dto.getGender() != null) {
            user.setGender(Gender.valueOf(dto.getGender().toUpperCase()));
            user.setGenderLocked(true);
        }

        // 생년월일: 잠겨있지 않을 때만 수정하고 잠금
        if (!user.isBirthLocked() && dto.getBirthDate() != null && !dto.getBirthDate().isBlank()) {
            try {
                user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
                user.setBirthLocked(true);
            } catch (Exception e) {
                // 잘못된 날짜 형식일 경우 예외 처리
            }
        }

        // MBTI 등은 잠금 없이 계속 수정 가능
        user.setMbti(dto.getMbti());

        // 기존 스타일 삭제
        user.getTravelStyles().clear();
        userRepository.saveAndFlush(user);

        user.setProfileImage(dto.getProfileImage());
        user.setAvatarEmoji(dto.getAvatarEmoji());
        user.setAvatarColor(dto.getAvatarColor());

        // 비워진 상태에서 새로운 스타일 추가
        if (dto.getTravelStyles() != null) {
            for (String styleName : dto.getTravelStyles()) {
                Style style = styleRepository.findByName(styleName)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 스타일: " + styleName));

                UserStyle userStyle = new UserStyle();
                userStyle.setUser(user);
                userStyle.setStyle(style);
                user.getTravelStyles().add(userStyle); // User 엔티티의 리스트에 추가
            }
        }
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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

        // 소셜 계정 연결 해제
        // social_accounts 테이블에서 해당 유저의 레코드를 삭제하거나 connected를 false로 변경합니다.
        // 삭제를 해야 나중에 동일한 소셜 계정으로 '새 가입'이 가능해집니다.
        socialAccountRepository.deleteByUser(user);

        userRepository.save(user);
    }

}
