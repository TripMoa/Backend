package com.tripmoa.expense.entity;

import com.tripmoa.expense.enums.DepositLogStatus;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "deposit_log",
        indexes = {
                @Index(name = "idx_deposit_trip_date", columnList = "trip_id, deposit_date"),
                @Index(name = "idx_deposit_member", columnList = "member_id"),
                @Index(name = "idx_deposit_created_by", columnList = "created_by_user_id"),
                @Index(name = "idx_deposit_status", columnList = "deposit_status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DepositLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // trip_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_deposit_trip"))
    private Trip trip;

    // trip_member : 실제 입금한 대상 멤버
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_deposit_member"))
    private TripMember member;

    // 입금 로그 등록자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_deposit_created_by_user"))
    private User createdBy;

    @Column(nullable = false)
    private int amount;

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;

    @Column(length = 120)
    private String memo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_status", nullable = false, length = 10)
    private DepositLogStatus depositStatus = DepositLogStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // === 메서드 ===
    public void confirm() {
        this.depositStatus = DepositLogStatus.CONFIRMED;
    }

    public void reject() {
        this.depositStatus = DepositLogStatus.REJECTED;
    }
}
