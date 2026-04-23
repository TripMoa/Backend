package com.tripmoa.report;

import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_reports")
@Getter
@NoArgsConstructor
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 신고당한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    // 신고당한 사람 닉네임 (탈퇴 대비)
    @Column(length = 50)
    private String reportedNickname;

    // 신고 위치 (POST, COMMENT, CHAT, MATE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportLocation location;

    // 대상 ID (chatRoomId, postId, commentId)
    @Column(nullable = false)
    private Long targetId;

    // 신고 사유 (라디오 선택값)
    @Column(nullable = false)
    private String reason;

    // 추가 설명 (선택)
    @Column(length = 500)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    // 스냅샷 방식
    @Column(length = 1000)
    private String contentSnapshot;

    @Builder
    public UserReport(User reporter, User reportedUser, String reportedNickname,
                      ReportLocation location,
                      Long targetId, String reason, String detail,
                      String contentSnapshot) {
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.reportedNickname = reportedNickname;
        this.location = location;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
        this.contentSnapshot = contentSnapshot;
        this.reportedAt = LocalDateTime.now();
    }
}