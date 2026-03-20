package com.tripmoa.expense.repository;

import com.tripmoa.expense.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    // 기본 findById() 사용
}