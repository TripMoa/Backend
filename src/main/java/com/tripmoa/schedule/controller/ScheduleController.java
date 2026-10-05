package com.tripmoa.schedule.controller;

import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.schedule.dto.ScheduleAutoComputeRequest;
import com.tripmoa.schedule.dto.ScheduleDayRequest;
import com.tripmoa.schedule.dto.ScheduleEstimateResponse;
import com.tripmoa.schedule.dto.ScheduleGenerateRequest;
import com.tripmoa.schedule.dto.ScheduleResponse;
import com.tripmoa.schedule.dto.ScheduleSettingsRequest;
import com.tripmoa.schedule.service.ScheduleComputeService;
import com.tripmoa.schedule.service.ScheduleService;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleComputeService scheduleComputeService;
    private final TripPermissionService tripPermissionService;

    // 일정 생성 (AI) POST /api/schedules/ai (Trip 오너 or 멤버만 가능)
    @PostMapping("/ai")
    public ResponseEntity<List<ScheduleResponse>> generate(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @RequestBody ScheduleGenerateRequest request) {

        Long userId = userDetails.getUser().getId();
        requireTripId(request.getTripId());
        tripPermissionService.assertOwnerOrMember(request.getTripId(), userId);

        List<ScheduleResponse> result = scheduleService.generateAndSave(request.getTripId(), toAiRequest(request));
        return ResponseEntity.ok(result);
    }

    // 일정 생성 전 예상 POST /api/schedules/estimate (Trip 오너 or 멤버만 가능) — 저장하지 않음
    @PostMapping("/estimate")
    public ResponseEntity<ScheduleEstimateResponse> estimate(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @RequestBody ScheduleGenerateRequest request) {

        Long userId = userDetails.getUser().getId();
        requireTripId(request.getTripId());
        tripPermissionService.assertOwnerOrMember(request.getTripId(), userId);

        return ResponseEntity.ok(scheduleService.estimate(toAiRequest(request)));
    }

    // tripId 없이 오면 "없는 여행(404)"이 아니라 요청 오류(400)로 알린다
    private static void requireTripId(Long tripId) {
        if (tripId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "tripId는 필수입니다.");
        }
    }

    // 프론트 요청을 그대로 Python으로 전달 (생성/예상이 같은 요청 형태를 쓴다)
    private AiScheduleRequest toAiRequest(ScheduleGenerateRequest request) {
        return AiScheduleRequest.builder()
                .places(request.getPlaces())
                .n_days(request.getN_days())
                .transportation_mode(request.getTransportation_mode())
                .start_date(request.getStart_date())
                .end_date(request.getEnd_date())
                .daily_start_time(request.getDaily_start_time())
                .daily_end_time(request.getDaily_end_time())
                .user_preferences(request.getUser_preferences())
                .pinned_places(request.getPinned_places())
                .hotels(request.getHotels())
                .departure_points(request.getDeparture_points())
                .build();
    }

    // 일차 만들기 POST /api/schedules/days (Trip 오너 or 멤버만 가능) — AI 생성 전에도 직접 노드를 넣을 수 있게 해당 일차 행을 보장
    @PostMapping("/days")
    public ResponseEntity<ScheduleResponse> ensureDay(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestBody ScheduleDayRequest request) {
        Long userId = userDetails.getUser().getId();
        requireTripId(request.getTripId());
        tripPermissionService.assertOwnerOrMember(request.getTripId(), userId);

        return ResponseEntity.ok(scheduleService.ensureDay(request.getTripId(), request.getDay()));
    }

    // 일정 조회 GET /api/schedules?tripId=1 (Trip 오너 or 멤버만 가능)
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestParam Long tripId) {
        Long userId = userDetails.getUser().getId();
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        return ResponseEntity.ok(scheduleService.getSchedules(tripId));
    }


    // 한 일차 조회 GET /api/schedules/{scheduleId}
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getDay(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleComputeService.getDay(userDetails.getUser().getId(), scheduleId));
    }

    // 자동 시각 계산 켜기/끄기 POST /api/schedules/{scheduleId}/auto-compute
    // 켜면 머무는 시간이 비어 있던 노드를 추정해 채우고 그날 시각을 엔진 값으로 다시 맞춘다
    @PostMapping("/{scheduleId}/auto-compute")
    public ResponseEntity<ScheduleResponse> autoCompute(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long scheduleId,
                                                        @RequestBody ScheduleAutoComputeRequest request) {
        if (request.getEnabled() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "enabled는 필수입니다.");
        }
        return ResponseEntity.ok(scheduleComputeService.setAutoCompute(
                userDetails.getUser().getId(), scheduleId, request.getEnabled()));
    }

    // 지금 상태로 시각 다시 계산 POST /api/schedules/{scheduleId}/recompute
    @PostMapping("/{scheduleId}/recompute")
    public ResponseEntity<ScheduleResponse> recompute(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleComputeService.recomputeNow(userDetails.getUser().getId(), scheduleId));
    }

    // 그날의 시작·종료 시각, 이동수단 변경 PATCH /api/schedules/{scheduleId}/settings
    @PatchMapping("/{scheduleId}/settings")
    public ResponseEntity<ScheduleResponse> updateSettings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable Long scheduleId,
                                                           @RequestBody ScheduleSettingsRequest request) {
        return ResponseEntity.ok(scheduleComputeService.updateSettings(
                userDetails.getUser().getId(), scheduleId, request));
    }
}
