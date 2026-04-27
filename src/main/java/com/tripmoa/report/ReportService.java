package com.tripmoa.report;

import com.tripmoa.chat.domain.ChatMessage;
import com.tripmoa.chat.repository.ChatMessageRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.report.dto.MyHiddenTargetsResponse;
import com.tripmoa.report.dto.MyReportHistoryResponse;
import com.tripmoa.report.dto.MyReportItemResponse;
import com.tripmoa.report.dto.ReportRequest;
import com.tripmoa.story.domain.StoryComment;
import com.tripmoa.story.repository.StoryCommentRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import com.tripmoa.user.service.UserSanctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserSanctionService userSanctionService;
    private final ChatMessageRepository chatMessageRepository;
    private final StoryCommentRepository commentRepository;

    @Transactional
    public void report(Long reporterId, ReportRequest request) {
        // 자기 자신 신고 방지
        if (reporterId.equals(request.getReportedUserId())) {
            throw new BusinessException(ErrorCode.CANNOT_REPORT_SELF);
        }

        // 중복 신고 방지
        boolean alreadyReported = reportRepository.existsByReporterIdAndLocationAndTargetId(
                reporterId,
                request.getLocation(),
                request.getTargetId()
        );

        // 댓글 실제 작성자를 조회 & 검증
        if (request.getLocation() == ReportLocation.COMMENT) {
            StoryComment comment = commentRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            if (!comment.getAuthor().getId().equals(request.getReportedUserId())) {
                throw new BusinessException(ErrorCode.INVALID_REPORT_TARGET);
            }
        }

        // 채팅 메세지 실제 작성자를 조회 & 검증
        if (request.getLocation() == ReportLocation.CHAT) {
            ChatMessage message = chatMessageRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

            if (!message.getSender().getId().equals(request.getReportedUserId())) {
                throw new BusinessException(ErrorCode.INVALID_REPORT_TARGET);
            }
        }

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
                .reportedNickname(reportedUser.getNickname())
                .location(request.getLocation())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .detail(request.getDetail())
                .build();

        try {
            reportRepository.save(report);
            userSanctionService.applySanction(reportedUser.getId());
        } catch (DataIntegrityViolationException e) {
            // 동시 요청 등으로 DB 유니크 제약에 걸린 경우도 중복 신고로 처리
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }
    }

    @Transactional(readOnly = true)
    public MyReportHistoryResponse getMyReportHistory(Long userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 5 : Math.min(size, 20);

        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<UserReport> reportPage = reportRepository
                .findAllByReportedUserIdOrderByReportedAtDesc(userId, pageable);

        long totalReportCount = reportPage.getTotalElements();
        int currentLevel = resolveLevel(totalReportCount);
        String currentLevelLabel = resolveLevelLabel(currentLevel);

        List<MyReportItemResponse> items = reportPage.getContent().stream()
                .map(report -> new MyReportItemResponse(
                        report.getId(),
                        report.getLocation().name(),
                        report.getTargetId(),
                        report.getReason(),
                        report.getDetail(),
                        report.getReportedNickname(),
                        report.getReportedAt()
                ))
                .toList();

        return new MyReportHistoryResponse(
                currentLevel,
                currentLevelLabel,
                totalReportCount,
                reportPage.getNumber(),
                reportPage.getTotalPages(),
                reportPage.getTotalElements(),
                reportPage.getSize(),
                items
        );
    }

    // 로그인한 사용자가 직접 신고한 대상 ID 목록 조회
    @Transactional(readOnly = true)
    public MyHiddenTargetsResponse getMyHiddenTargets(Long reporterId, ReportLocation location) {
        userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Long> targetIds = reportRepository
                .findDistinctTargetIdsByReporterIdAndLocation(reporterId, location);

        return new MyHiddenTargetsResponse(location.name(), targetIds);
    }

    @Transactional(readOnly = true)
    public ContentStatus resolveContentStatus(ReportLocation location, Long targetId) {
        long reportCount = reportRepository.countByLocationAndTargetId(location, targetId);
        return reportCount >= 3 ? ContentStatus.REPORTED : ContentStatus.NORMAL;
    }

    @Transactional(readOnly = true)
    public String resolveDisplayContent(ContentStatus status, String originalContent) {
        return status == ContentStatus.REPORTED
                ? "신고된 메시지입니다."
                : originalContent;
    }

    @Transactional(readOnly = true)
    public String resolveWriterName(ContentStatus status, String originalName) {
        return status == ContentStatus.REPORTED
                ? "작성자 미상"
                : originalName;
    }

    private int resolveLevel(long totalReportCount) {
        if (totalReportCount >= 15) return 4;
        if (totalReportCount >= 10) return 3;
        if (totalReportCount >= 5) return 2;
        if (totalReportCount >= 1) return 1;
        return 0;
    }

    private String resolveLevelLabel(int level) {
        return switch (level) {
            case 1 -> "1단계";
            case 2 -> "2단계";
            case 3 -> "3단계";
            case 4 -> "4단계";
            default -> "정상";
        };
    }

}