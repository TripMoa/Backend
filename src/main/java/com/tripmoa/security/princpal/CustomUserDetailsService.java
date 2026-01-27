package com.tripmoa.security.princpal;

import com.tripmoa.user.entity.User;
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
                .orElseThrow(() ->
                        new RuntimeException("사용자를 찾을 수 없습니다. id=" + userId)
                );

        // User 엔티티를 CustomUserDetails로 감싸서 반환
        return new CustomUserDetails(user);
    }
}

