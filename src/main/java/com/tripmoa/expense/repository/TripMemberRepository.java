package com.tripmoa.expense.repository;

import com.tripmoa.expense.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    // 특정 사용자가 해당 Trip의 멤버인지 여부 확인
    boolean existsByTrip_IdAndUser_Id(Long tripId, Long userId);

    // 특정 TripMember(id)가 해당 Trip(tripId)에 속해있는지 여부 확인
    boolean existsByIdAndTrip_Id(Long id, Long tripId);

    // 특정 여행(tripId)의 멤버 목록을 정렬 순서(sortOrder) 기준으로 조회
    List<TripMember> findAllByTrip_IdOrderBySortOrderAsc(Long tripId);

}