package com.tripmoa.security.principal;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰에 들어있는 userId를 기준으로 우리 서비스의 User 엔티티를 조회하는 클래스
 * 조회된 User를 CustomUserDetails로 감싸서 Spring Security에 전달하는 역할을 한다.
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    /**
     * userId로 사용자 조회
     *
     * @param userId JWT 토큰에서 추출한 사용자 ID
     * @return CustomUserDetails (SecurityContext에 저장될 객체)
     */
    public CustomUserDetails loadUserById(Long userId) {

        // DB에서 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 정지/탈퇴 등 비활성 상태 사용자는 인증 불가
        validateActiveUser(user);

        // User 엔티티를 CustomUserDetails로 감싸서 반환
        return new CustomUserDetails(user);
    }

    /**
     * 현재 로그인 가능한 상태의 사용자만 통과
     * - ACTIVE     : 정상 이용 가능
     * - SUSPENDED  : 신고 정책에 따른 계정 정지
     * - WITHDRAWN  : 탈퇴 사용자
     */
    private void validateActiveUser(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_FORBIDDEN);
        }
    }
}

