package com.tripmoa.schedule.repository;

import com.tripmoa.schedule.domain.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ScheduleItemRepository
 * - 타임라인 조회
 */
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

    // 특정 Day의 타임라인
    List<ScheduleItem> findAllByScheduleId(Long scheduleId);

    // 특정 Day의 타임라인 전체 삭제 (일정 재생성 시 사용)
    void deleteAllByScheduleId(Long scheduleId);
}
