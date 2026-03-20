package com.tripmoa.expense.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "expense_split",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_split_expense_member", columnNames = {"expense_id", "member_id"})
        },
        indexes = {
                @Index(name = "idx_split_member", columnList = "member_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // expense_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false, foreignKey = @ForeignKey(name = "fk_split_expense"))
    private Expense expense;

    // member_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_split_member"))
    private TripMember member;

    @Column(nullable = false)
    private int amount;

    // Expense에서 메서드로 세팅할 수 있게 setter 열어둠
    void setExpense(Expense expense) {
        this.expense = expense;
    }

    // === 메서드 ===
    public void changeAmount(int amount) {
        this.amount = amount;
    }
}
