package com.tripmoa.expense.repository;

import com.tripmoa.expense.entity.SettlementSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementSettingRepository extends JpaRepository<SettlementSetting, Long> {

    // Trip 1건에 대한 정산 설정(1:1) 조회
    Optional<SettlementSetting> findByTrip_Id(Long tripId);
}