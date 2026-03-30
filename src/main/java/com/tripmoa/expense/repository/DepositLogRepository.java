package com.tripmoa.expense.repository;

import com.tripmoa.expense.entity.DepositLog;
import com.tripmoa.expense.enums.DepositLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositLogRepository extends JpaRepository<DepositLog, Long> {

    // 특정 여행의 전체 입금 로그 조회
    List<DepositLog> findAllByTrip_Id(Long tripId);

    // 특정 여행의 특정 멤버 입금 로그 조회
    List<DepositLog> findAllByTrip_IdAndMember_Id(Long tripId, Long memberId);

    // 특정 여행의 특정 멤버 입금 로그를 입금일 및 생성일 최신순으로 조회
    List<DepositLog> findAllByTrip_IdAndMember_IdOrderByDepositDateDescCreatedAtDesc(
            Long tripId,
            Long memberId
    );

    // 특정 여행의 특정 상태 입금 로그 조회
    List<DepositLog> findAllByTrip_IdAndDepositStatus(Long tripId, DepositLogStatus depositStatus);

}