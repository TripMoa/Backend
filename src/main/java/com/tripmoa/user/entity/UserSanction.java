package com.tripmoa.user.entity;

import com.tripmoa.user.enums.SanctionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 제재 상태

@Entity
@Table(name = "user_sanctions")
@Getter
@Setter
public class UserSanction {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer level = 0;

    @Column(name = "total_reports", nullable = false)
    private Integer totalReports = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SanctionStatus status = SanctionStatus.NORMAL;

    @Column(name = "warning_popup_checked_at")
    private LocalDateTime warningPopupCheckedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markWarningPopupChecked() {
        this.warningPopupCheckedAt = LocalDateTime.now();
    }
}