package com.tripmoa.trip.repository;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // 활성화된 여행 조회
    Optional<Trip> findByIdAndStatus(Long id, TripStatus status);

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