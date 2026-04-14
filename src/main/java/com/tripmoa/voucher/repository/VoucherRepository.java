package com.tripmoa.voucher.repository;

import com.tripmoa.voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // 특정 여행의 바우처 전체 조회 (최신순)
    List<Voucher> findAllByTrip_IdOrderByCreatedAtDesc(Long tripId);

    // 특정 여행에 속한 특정 바우처 조회
    Optional<Voucher> findByIdAndTrip_Id(Long voucherId, Long tripId);
}