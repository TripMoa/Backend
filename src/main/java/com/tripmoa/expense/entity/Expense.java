package com.tripmoa.expense.entity;

import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;
import com.tripmoa.expense.enums.SplitMode;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "expense",
        indexes = {
                @Index(name = "idx_expense_trip_paidat", columnList = "trip_id, paid_at"),
                @Index(name = "idx_expense_trip_created", columnList = "trip_id, created_at"),
                @Index(name = "idx_expense_payer", columnList = "payer_member_id"),
                @Index(name = "idx_expense_created_by", columnList = "created_by_user_id"),
                @Index(name = "idx_expense_trip_shared", columnList = "trip_id, is_shared"),
                @Index(name = "idx_expense_trip_category", columnList = "trip_id, category"),
                @Index(name = "idx_expense_trip_cat_paidat", columnList = "trip_id, category, paid_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // trip_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_trip"))
    private Trip trip;

    // created_by_user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_created_by"))
    private User createdBy;

    // payer_member_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_payer_member"))
    private TripMember payerMember;

    @Builder.Default
    @Column(name = "auto_include_payer", nullable = false)
    private boolean autoIncludePayer = true;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "store_name", length = 80, nullable = false)
    private String storeName;

    @Column(name = "item_memo", length = 120)
    private String itemMemo;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    // 공동 지출 여부
    @Builder.Default
    @Column(name = "is_shared", nullable = false)
    private boolean shared = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_method", nullable = false, length = 10)
    private PayMethod payMethod;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "receipt_file_name", length = 255)
    private String receiptFileName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "split_mode", nullable = false, length = 10)
    private SplitMode splitMode = SplitMode.EQUAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === 연관관계 ===
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseSplit> splits = new ArrayList<>();

    // === 메서드 ===
    @PrePersist
    public void prePersist() {
        if (splitMode == null) splitMode = SplitMode.EQUAL;
        // boolean은 null이 없어서(primitive) 필요 없지만, wrapper(Boolean)면 처리
    }

    public void addSplit(ExpenseSplit split) {
        splits.add(split);
        split.setExpense(this);
    }

    public void removeSplit(ExpenseSplit split) {
        splits.remove(split);
        split.setExpense(null);
    }

    public void clearSplits() {
        for (ExpenseSplit split : new ArrayList<>(splits)) {
            removeSplit(split);
        }
    }

    public void updateExpense(
            TripMember payerMember,
            boolean autoIncludePayer,
            LocalDateTime paidAt,
            String storeName,
            String itemMemo,
            int totalAmount,
            ExpenseCategory category,
            PayMethod payMethod,
            boolean shared,
            String receiptUrl,
            String receiptFileName,
            SplitMode splitMode
    ) {
        this.payerMember = payerMember;
        this.autoIncludePayer = autoIncludePayer;
        this.paidAt = paidAt;
        this.storeName = storeName;
        this.itemMemo = itemMemo;
        this.totalAmount = totalAmount;
        this.category = category;
        this.payMethod = payMethod;
        this.shared = shared;
        this.receiptUrl = receiptUrl;
        this.receiptFileName = receiptFileName;
        this.splitMode = splitMode;
    }
}