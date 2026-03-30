package com.tripmoa.trip.repository;

import com.tripmoa.trip.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    // 권한 체크용 : 특정 사용자가 해당 Trip의 멤버인지 여부 확인
    boolean existsByTrip_IdAndUser_Id(Long tripId, Long userId);

    // 특정 멤버 검증용 : 특정 TripMember(id)가 해당 Trip(tripId)에 속해있는지 여부 확인
    boolean existsByIdAndTrip_Id(Long id, Long tripId);

    // 특정 여행(tripId)의 멤버 목록을 정렬 순서(sortOrder) 기준으로 조회
    List<TripMember> findAllByTrip_IdOrderBySortOrderAsc(Long tripId);

    // 특정 멤버가 해당 여행에 속해있는지 조회
    Optional<TripMember> findByIdAndTrip_Id(Long memberId, Long tripId);

    // 같은 여행 내에서 닉네임 중복 여부 확인
    boolean existsByTrip_IdAndNickname(Long tripId, String nickname);

    // 여행 목록 조회 시 여행마다 멤버를 개별 조회하지 않고 한 번에 가져와 N+1 문제를 방지하기 위한 메서드
    List<TripMember> findAllByTrip_IdInOrderByTrip_IdAscSortOrderAsc(List<Long> tripIds);

}