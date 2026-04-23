package com.tripmoa.report;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private static final int SNAPSHOT_MAX_LENGTH = 200;

    @Transactional
    public void report(Long reporterId, ReportRequest request) {
        // 자기 자신 신고 방지
        if (reporterId.equals(request.getReportedUserId())) {
            throw new BusinessException(ErrorCode.CANNOT_REPORT_SELF);
        }

        // 중복 신고 방지
        boolean alreadyReported = reportRepository
                .existsByReporterIdAndLocationAndTargetId(reporterId, request.getLocation(), request.getTargetId());

        if (alreadyReported) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORTED_USER_NOT_FOUND));

        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportedNickname(request.getReportedNickname())
                .location(request.getLocation())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .detail(request.getDetail())
                .contentSnapshot(truncate(request.getContentSnapshot()))
                .build();

        reportRepository.save(report);
    }

    private String truncate(String text) {
        if (text == null) return null;
        if (text.length() <= SNAPSHOT_MAX_LENGTH) return text;
        return text.substring(0, SNAPSHOT_MAX_LENGTH) + "...";
    }
}

