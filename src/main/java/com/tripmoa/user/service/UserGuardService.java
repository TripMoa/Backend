package com.tripmoa.user.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserGuardService {

    private final UserRepository userRepository;

    // 사용자 존재 확인, 없으면 404
    public User getUserOr404(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 활성 사용자 확인, 아니면 접근 불가
    public User getActiveUserOr403(Long userId) {
        User user = getUserOr404(userId);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_FORBIDDEN);
        }

        return user;
    }

    // 생년월일 등록 여부 확인
    public void assertBirthDateExists(Long userId) {
        User user = getActiveUserOr403(userId);

        if (user.getBirthDate() == null) {
            throw new BusinessException(ErrorCode.BIRTH_DATE_REQUIRED);
        }
    }

    // 성인 인증 여부 확인
    public void assertAdultVerified(Long userId) {
        User user = getActiveUserOr403(userId);

        if (!Boolean.TRUE.equals(user.getAgeVerified())) {
            throw new BusinessException(ErrorCode.ADULT_VERIFICATION_REQUIRED);
        }
    }

    // 생년월일 등록 여부와 성인 인증 여부 확인이 함께 필요한 경우
    public void assertAdultAccess(Long userId) {
        User user = getActiveUserOr403(userId);

        if (user.getBirthDate() == null) {
            throw new BusinessException(ErrorCode.BIRTH_DATE_REQUIRED);
        }

        if (!user.isAdultByBirthDate()) {
            throw new BusinessException(ErrorCode.UNDERAGE_USER);
        }

        if (!Boolean.TRUE.equals(user.getAgeVerified())) {
            throw new BusinessException(ErrorCode.ADULT_VERIFICATION_REQUIRED);
        }
    }
}