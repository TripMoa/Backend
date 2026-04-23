package com.tripmoa.place.repository;

import com.tripmoa.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * PlaceRepository
 * - 장소 DB 접근
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 특정 여행의 장소 목록 조회
    List<Place> findAllByTripId(Long tripId);
}
