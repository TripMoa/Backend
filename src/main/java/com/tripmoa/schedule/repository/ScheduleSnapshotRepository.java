package com.tripmoa.schedule.repository;

import com.tripmoa.schedule.domain.ScheduleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleSnapshotRepository extends JpaRepository<ScheduleSnapshot, Long> {

    Optional<ScheduleSnapshot> findByTripId(Long tripId);

    void deleteByTripId(Long tripId);
}
