package com.tripmoa.expense.entity;

import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.PoolBalancePolicy;
import com.tripmoa.expense.enums.SplitRemainderPolicy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_setting",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_settlement_trip", columnNames = "trip_id")
        },
        indexes = {
                @Index(name = "idx_settlement_trip", columnList = "trip_id"),
                @Index(name = "idx_settlement_mode", columnList = "payment_mode")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SettlementSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 (trip_id UNIQUE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_settlement_trip"))
    private Trip trip;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private PaymentMode paymentMode = PaymentMode.HYBRID;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_remainder_policy", length = 30)
    private SplitRemainderPolicy splitRemainderPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "pool_balance_policy", length = 30)
    private PoolBalancePolicy poolBalancePolicy;

    @Column(name = "budget_amount", nullable = false)
    private int budgetAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Trip에서 메서드로 세팅할 수 있게 setter 열어둠
    void setTrip(Trip trip) {
        this.trip = trip;
    }

    // === 메서드 ===
    @PrePersist
    public void prePersist() {
        if (paymentMode == null) paymentMode = PaymentMode.HYBRID;
    }

    public void changePaymentMode(PaymentMode mode) {
        this.paymentMode = mode;
    }

    public void changeBudgetAmount(int budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public void changeSplitRemainderPolicy(SplitRemainderPolicy policy) {
        this.splitRemainderPolicy = policy;
    }

    public void changePoolBalancePolicy(PoolBalancePolicy policy) {
        this.poolBalancePolicy = policy;
    }
}
