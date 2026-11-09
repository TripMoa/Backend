package com.tripmoa.schedule.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.ExcludedPlaceResponse;
import com.tripmoa.schedule.dto.ScheduleItemResponse;
import com.tripmoa.schedule.dto.ScheduleResponse;
import com.tripmoa.schedule.dto.ScheduleWarningResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Schedule(+노드) 엔티티 → 응답 DTO
 * - 조회·일차 만들기·생성 응답이 모두 같은 모양을 돌려주도록 한 곳에서 만든다
 * - 경고는 마지막 자동 계산 결과(warningsJson)를 풀어서 노드별/날 단위로 나눈다.
 *   자동 계산이 꺼진 날은 낡은 경고를 보여주지 않는다.
 */
public final class ScheduleResponseMapper {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> WARNING_LIST = new TypeReference<>() {};
    private static final int LAST_MINUTE_OF_DAY = 23 * 60 + 59;

    private ScheduleResponseMapper() {
    }

    public static ScheduleResponse toResponse(
            Schedule schedule,
            List<ScheduleItem> items,
            List<String> pinWarnings,
            List<ExcludedPlaceResponse> excludedPlaces
    ) {
        List<ScheduleWarningResponse> all = schedule.isAutoComputeOn()
                ? parseWarnings(schedule.getWarningsJson())
                : List.of();

        List<ScheduleWarningResponse> dayWarnings = all.stream().filter(w -> w.getItemId() == null).toList();
        int over = dayWarnings.stream()
                .map(ScheduleWarningResponse::getOverMinutes)
                .filter(m -> m != null)
                .findFirst()
                .orElse(0);

        List<ScheduleItemResponse> itemResponses = items.stream()
                .sorted(Comparator.comparingInt(ScheduleItem::getOrderIndex).thenComparing(ScheduleItem::getId))
                .map(item -> toItemResponse(item, all.stream()
                        .filter(w -> item.getId().equals(w.getItemId()))
                        .toList()))
                .toList();

        return ScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .day(schedule.getDay())
                .items(itemResponses)
                .pinWarnings(pinWarnings != null ? pinWarnings : Collections.emptyList())
                .excludedPlaces(excludedPlaces != null ? excludedPlaces : Collections.emptyList())
                .autoCompute(schedule.isAutoComputeOn())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .transportMode(schedule.getTransportMode())
                .overMinutes(over)
                .warnings(dayWarnings)
                .build();
    }

    private static ScheduleItemResponse toItemResponse(ScheduleItem item, List<ScheduleWarningResponse> warnings) {
        return ScheduleItemResponse.builder()
                .id(item.getId())
                .time(item.getTime())
                .title(item.getTitle())
                .category(item.getCategory())
                .description(item.getDescription())
                .orderIndex(item.getOrderIndex())
                .lat(item.getLat())
                .lng(item.getLng())
                .travelMinutes(item.getTravelMinutes())
                .travelPayment(item.getTravelPayment())
                .travelTransfer(item.getTravelTransfer())
                .stayMinutes(item.getStayMinutes())
                .endTime(endTimeOf(item.getTime(), item.getStayMinutes()))
                .pinnedTime(item.getPinnedTime())
                .memo(item.getMemo())
                .warnings(warnings)
                .build();
    }

    /** 시작 시각 + 머무는 시간 (24시를 넘기면 23:59로 — 엔진과 같은 규칙) */
    static String endTimeOf(String time, Integer stayMinutes) {
        Integer start = toMinutes(time);
        if (start == null || stayMinutes == null) return null;
        int end = Math.min(start + stayMinutes, LAST_MINUTE_OF_DAY);
        return String.format("%02d:%02d", end / 60, end % 60);
    }

    static Integer toMinutes(String hhmm) {
        if (hhmm == null || !hhmm.matches("\\d{1,2}:\\d{2}")) return null;
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private static List<ScheduleWarningResponse> parseWarnings(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<ScheduleWarningResponse> out = new ArrayList<>();
            for (Map<String, Object> w : JSON.readValue(json, WARNING_LIST)) {
                out.add(ScheduleWarningResponse.builder()
                        .itemId(asLong(w.get("itemId")))
                        .code(String.valueOf(w.get("code")))
                        .message(String.valueOf(w.get("message")))
                        .overMinutes(w.get("overMinutes") instanceof Number n ? n.intValue() : null)
                        .suggestMoveItemId(asLong(w.get("suggestMoveItemId")))
                        .build());
            }
            return out;
        } catch (Exception e) {
            return List.of();   // 깨진 경고는 화면을 막지 않는다 — 다음 계산 때 다시 채워진다
        }
    }

    private static Long asLong(Object v) {
        return v instanceof Number n ? n.longValue() : null;
    }
}
