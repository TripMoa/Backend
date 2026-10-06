package com.tripmoa.voucher.repository;

import com.tripmoa.voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // 특정 여행의 바우처 전체 조회 (최신순)
    List<Voucher> findAllByTrip_IdOrderByCreatedAtDesc(Long tripId);

    // 특정 여행에서, 특정 일정 항목에 연결된 바우처만 조회 (최신순)
    List<Voucher> findAllByTrip_IdAndScheduleItemIdOrderByCreatedAtDesc(Long tripId, Long scheduleItemId);

    // 특정 여행에 속한 특정 바우처 조회
    Optional<Voucher> findByIdAndTrip_Id(Long voucherId, Long tripId);

    // 일정 항목이 삭제됐을 때, 거기 연결돼 있던 바우처들의 연결을 끊음 (바우처 자체는 유지)
    @Modifying
    @Query("UPDATE Voucher v SET v.scheduleItemId = NULL WHERE v.scheduleItemId = :scheduleItemId")
    void unlinkScheduleItem(@Param("scheduleItemId") Long scheduleItemId);
}
