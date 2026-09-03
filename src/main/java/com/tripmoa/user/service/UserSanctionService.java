package com.tripmoa.user.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.report.ReportRepository;
import com.tripmoa.user.dto.MySanctionStatusResponse;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.entity.UserSanction;
import com.tripmoa.user.enums.SanctionStatus;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.UserRepository;
import com.tripmoa.user.repository.UserSanctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSanctionService {

    private final ReportRepository reportRepository;
    private final UserSanctionRepository sanctionRepository;
    private final UserRepository userRepository;

    /**
     * 신고 발생 시 호출되는 메서드
     */
    @Transactional
    public void applySanction(Long userId) {

        // 현재까지 누적 신고 횟수 조회
        long totalReports = reportRepository.countByReportedUserId(userId);

        // 신고 횟수 → 단계 계산 → 상태(NORMAL / WARNING / SUSPENDED) 변환
        int level = resolveLevel(totalReports);
        SanctionStatus status = resolveStatus(level);

        // 기존 제재 상태 조회 (없으면 생성)
        UserSanction sanction = sanctionRepository.findById(userId)
                .orElse(null);

        if (sanction == null) {
            sanction = create(userId);
            sanctionRepository.save(sanction);
        }

        sanction.setTotalReports((int) totalReports);
        sanction.setLevel(level);

        // 상태가 변경된 경우에만 처리
        if (!sanction.getStatus().equals(status)) {
            sanction.setStatus(status);

            // 4단계 → 유저 정지
            if (status == SanctionStatus.SUSPENDED) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                user.setStatus(UserStatus.SUSPENDED);
            }
        }

    }

    /**
     * 로그인한 사용자의 현재 제재 상태 조회
     */
    @Transactional
    public MySanctionStatusResponse getMySanctionStatus(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        applySanction(userId);

        UserSanction sanction = sanctionRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_SANCTION_NOT_FOUND));

        boolean showWarningPopup = shouldShowWarningPopup(sanction);

        String warningMessage = showWarningPopup
                ? "신고가 누적되었습니다. 반복될 경우 서비스 이용이 제한될 수 있습니다."
                : null;

        return new MySanctionStatusResponse(
                sanction.getLevel(),
                sanction.getTotalReports(),
                sanction.getStatus().name(),
                showWarningPopup,
                warningMessage
        );
    }

    @Transactional
    public void markWarningPopupRead(Long userId) {
        UserSanction sanction = sanctionRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_SANCTION_NOT_FOUND));

        sanction.markWarningPopupChecked();
    }

    /**
     * user_sanctions 최초 생성
     */
    private UserSanction create(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserSanction sanction = new UserSanction();
        sanction.setUser(user);
        sanction.setLevel(0);
        sanction.setTotalReports(0);
        sanction.setStatus(SanctionStatus.NORMAL);

        return sanction;
    }

    // 신고 횟수 → 제재 단계 변환
    private int resolveLevel(long count) {
        if (count >= 15) return 4;
        if (count >= 10) return 3;
        if (count >= 5) return 2;
        if (count >= 1) return 1;
        return 0;
    }

    // 단계 → 상태 변환
    private SanctionStatus resolveStatus(int level) {
        if (level >= 4) return SanctionStatus.SUSPENDED;
        if (level >= 2) return SanctionStatus.WARNING;
        return SanctionStatus.NORMAL;
    }

    // 경고 팝업 노출 여부 판단
    private boolean shouldShowWarningPopup(UserSanction sanction) {
        if (sanction.getLevel() < 3) {
            return false;
        }

        if (sanction.getStatus() == SanctionStatus.SUSPENDED) {
            return false;
        }

        LocalDateTime checkedAt = sanction.getWarningPopupCheckedAt();

        if (checkedAt == null) {
            return true;
        }

        return checkedAt.toLocalDate().isBefore(LocalDate.now());
    }
}