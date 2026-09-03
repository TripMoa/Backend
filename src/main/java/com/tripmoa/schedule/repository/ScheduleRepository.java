package com.tripmoa.schedule.repository;

import com.tripmoa.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ScheduleRepository
 * - Day 단위 일정 조회
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 여행 전체 일정 조회
    List<Schedule> findAllByTripId(Long tripId);

}
