package com.tripmoa.trip.repository;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // 활성화된 여행 조회
    Optional<Trip> findByIdAndStatus(Long id, TripStatus status);

    // 초대코드로 활성화된 여행 조회
    Optional<Trip> findByInviteCodeAndStatus(String inviteCode, TripStatus status);

    // 인원을 세고 멤버를 넣는 동안 같은 여행에 대한 다른 요청이 끼어들지 못하게 여행 행을 잠그고 조회한다 (SELECT ... FOR UPDATE).
    // 잠금이 풀리는 건 호출한 트랜잭션이 끝날 때이고, 그때까지 같은 여행으로 들어온 요청은 여기서 기다린다.
    // 주의: 이 트랜잭션의 첫 번째 조회여야 한다 — 먼저 일반 조회를 하면 그 시점의 스냅샷(REPEATABLE READ)으로 인원을 세어
    // 기다리는 동안 다른 요청이 넣은 멤버를 못 본다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.inviteCode = :inviteCode and t.status = :status")
    Optional<Trip> findByInviteCodeAndStatusForUpdate(@Param("inviteCode") String inviteCode,
                                                      @Param("status") TripStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :id and t.status = :status")
    Optional<Trip> findByIdAndStatusForUpdate(@Param("id") Long id, @Param("status") TripStatus status);

    boolean existsByInviteCode(String inviteCode);

    // 내가 소유한 여행 전체 조회
    List<Trip> findAllByOwner_IdAndStatusOrderByCreatedAtDesc(Long userId, TripStatus status);

    // 내가 소유한 여행 + 공개 여부 필터 조회
    List<Trip> findAllByOwner_IdAndVisibilityAndStatusOrderByCreatedAtDesc(
            Long userId,
            TripVisibility visibility,
            TripStatus status
    );

    // 내가 멤버로 참여한 여행 목록 조회 (오너인 여행 제외)
    @Query("""
        select distinct t
        from Trip t
        join TripMember tm on tm.trip.id = t.id
        where tm.user.id = :userId
          and t.owner.id <> :userId
          and t.status = :tripStatus
        order by t.createdAt desc
    """)
    List<Trip> findAllInvitedTripsByUserIdAndStatus(@Param("userId") Long userId,
                                                    @Param("tripStatus") TripStatus tripStatus);

}