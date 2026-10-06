package com.tripmoa.schedule.repository;

import com.tripmoa.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * ScheduleRepository
 * - Day 단위 일정 조회
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 여행 전체 일정 조회
    List<Schedule> findAllByTripId(Long tripId);

    // 특정 여행의 특정 일차 (혹시 중복 행이 있어도 가장 먼저 만들어진 것 하나만)
    Optional<Schedule> findFirstByTripIdAndDayOrderByIdAsc(Long tripId, int day);

}
