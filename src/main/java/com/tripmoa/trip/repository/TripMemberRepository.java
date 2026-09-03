package com.tripmoa.trip.repository;

import com.tripmoa.trip.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 특정 여행의 멤버 수 조회
    int countByTrip_Id(Long tripId);

    // 특정 여행에 아직 멤버가 남아있는지 확인
    boolean existsByTrip_Id(Long tripId);

    // 특정 여행에서 특정 유저의 TripMember 조회
    Optional<TripMember> findByTrip_IdAndUser_Id(Long tripId, Long userId);

    // 나가는/삭제되는 멤버를 제외하고 다음 소유주 후보 조회
    Optional<TripMember> findFirstByTrip_IdAndIdNotAndUserIsNotNullOrderBySortOrderAsc(
            Long tripId,
            Long memberId
    );

    // 회원 탈퇴 시, 해당 유저가 참여 중이던 모든 여행 멤버 닉네임을 익명 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TripMember tm
           set tm.nickname = :nickname
         where tm.user.id = :userId
    """)
    void updateNicknameByUserId(
            @Param("userId") Long userId,
            @Param("nickname") String nickname
    );

}