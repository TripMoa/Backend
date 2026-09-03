package com.tripmoa.report;

import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_reports_report_once",
                        columnNames = {"reporter_id", "location", "target_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_reports_reported_user_id", columnList = "reported_user_id"),
                @Index(name = "idx_user_reports_target", columnList = "location, target_id"),
                @Index(name = "idx_user_reports_reported_at", columnList = "reported_at")
        }
)
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

    // 신고 당시 닉네임 (닉변/탈퇴 대비)
    @Column(name = "reported_nickname", length = 50)
    private String reportedNickname;

    // 신고 위치
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportLocation location;

    // 신고 대상 ID
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // 신고 사유
    @Column(nullable = false, length = 100)
    private String reason;

    // 추가 설명
    @Column(length = 500)
    private String detail;

    // 신고 시각
    @CreationTimestamp
    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Builder
    public UserReport(User reporter, User reportedUser, String reportedNickname,
                      ReportLocation location, Long targetId, String reason,
                      String detail) {
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.reportedNickname = reportedNickname;
        this.location = location;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
    }
}