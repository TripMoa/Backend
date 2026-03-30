package com.tripmoa.expense.repository;

import com.tripmoa.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 특정 여행의 전체 지출 조회
    List<Expense> findAllByTrip_Id(Long tripId);

    // 특정 여행의 공동 지출만 조회
    List<Expense> findAllByTrip_IdAndSharedTrue(Long tripId);

    // 특정 여행의 개인 지출만 조회
    List<Expense> findAllByTrip_IdAndSharedFalse(Long tripId);

    // 특정 여행(tripId)의 전체 지출을 결제일 최신순, 생성일 최신순으로 조회
    List<Expense> findAllByTrip_IdOrderByPaidAtDescCreatedAtDesc(Long tripId);

    // 특정 여행의 지출 목록을 연관 엔티티와 함께 조회 (N+1 문제 방지)
    @Query("""
            select distinct e
            from Expense e
            left join fetch e.payerMember pm
            left join fetch e.createdBy cb
            left join fetch e.splits s
            left join fetch s.member sm
            where e.trip.id = :tripId
            order by e.paidAt desc, e.createdAt desc
            """)
    List<Expense> findAllWithDetailsByTripId(@Param("tripId") Long tripId);

    // 특정 여행의 지출 상세 1건 조회 (N+1 문제 방지)
    @Query("""
            select distinct e
            from Expense e
            join fetch e.trip t
            join fetch e.payerMember pm
            left join fetch e.splits s
            left join fetch s.member m
            where e.id = :expenseId
              and t.id = :tripId
            """)
    Optional<Expense> findDetailByTripIdAndExpenseId(Long tripId, Long expenseId);

}
