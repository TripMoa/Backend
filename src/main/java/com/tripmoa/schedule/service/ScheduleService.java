package com.tripmoa.schedule.service;

import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.ai.dto.AiScheduleResponse;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.ScheduleItemResponse;
import com.tripmoa.schedule.dto.ScheduleResponse;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DB 저장 + 조회 담당
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleAiService scheduleAiService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;

    /**
     * AI 일정 생성 후 DB 저장
     * - 기존 일정이 있으면 삭제 후 재생성 (중복 방지)
     */
    @Transactional
    public List<ScheduleResponse> generateAndSave(Long tripId, AiScheduleRequest request) {

        // 기존 일정 삭제 (재생성 케이스 대비)
        List<Schedule> existing = scheduleRepository.findAllByTripId(tripId);
        if (!existing.isEmpty()) {
            List<Long> scheduleIds = existing.stream().map(Schedule::getId).toList();
            scheduleIds.forEach(scheduleItemRepository::deleteAllByScheduleId);
            scheduleRepository.deleteAll(existing);
        }

        // AI 호출
        AiScheduleResponse response = scheduleAiService.response(request);

        // Schedule 저장 — saveAll() 반환값(ID가 채워진 객체)을 사용해야 함
        List<Schedule> schedules = ScheduleMapper.toSchedules(tripId, response);
        List<Schedule> savedSchedules = scheduleRepository.saveAll(schedules);

        // ScheduleItem 저장 — savedSchedules의 ID를 사용해야 scheduleId가 null이 아님
        List<AiScheduleResponse.DayPlan> dayPlans = response.getItinerary().getDays();
        for (int i = 0; i < savedSchedules.size(); i++) {
            Schedule savedSchedule = savedSchedules.get(i);
            AiScheduleResponse.DayPlan dayPlan = dayPlans.get(i);
            List<ScheduleItem> items = ScheduleMapper.toScheduleItems(savedSchedule.getId(), dayPlan);
            scheduleItemRepository.saveAll(items);
        }

        // 저장된 일정을 바로 반환 (Controller에서 프론트로 내려줌)
        return getSchedules(tripId);
    }

    /**
     * 특정 여행의 전체 일정 조회
     * - day 오름차순, 각 day의 아이템은 orderIndex 오름차순
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long tripId) {

        List<Schedule> schedules = scheduleRepository.findAllByTripId(tripId);

        return schedules.stream()
                .sorted((a, b) -> Integer.compare(a.getDay(), b.getDay()))
                .map(schedule -> {
                    List<ScheduleItemResponse> items = scheduleItemRepository
                            .findAllByScheduleId(schedule.getId())
                            .stream()
                            .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                            .map(item -> ScheduleItemResponse.builder()
                                    .id(item.getId())
                                    .time(item.getTime())
                                    .title(item.getTitle())
                                    .category(item.getCategory())
                                    .description(item.getDescription())
                                    .orderIndex(item.getOrderIndex())
                                    .lat(item.getLat())
                                    .lng(item.getLng())
                                    .build())
                            .toList();

                    return ScheduleResponse.builder()
                            .scheduleId(schedule.getId())
                            .day(schedule.getDay())
                            .items(items)
                            .build();
                })
                .toList();
    }
}