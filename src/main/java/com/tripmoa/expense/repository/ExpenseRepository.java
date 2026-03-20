package com.tripmoa.expense.repository;

import com.tripmoa.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 특정 여행의 전체 지출 조회
    List<Expense> findAllByTrip_Id(Long tripId);

    // 특정 여행의 공동 지출만 조회
    List<Expense> findAllByTrip_IdAndSharedTrue(Long tripId);

    // 특정 여행의 개인 지출만 조회
    List<Expense> findAllByTrip_IdAndSharedFalse(Long tripId);

}
