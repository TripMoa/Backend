package com.tripmoa.schedule.controller;

import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.schedule.dto.ScheduleGenerateRequest;
import com.tripmoa.schedule.dto.ScheduleResponse;
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
//    private final TripPermissionService tripPermissionService;

    //  일정 생성 (AI) POST /api/schedules/ai(Trip 오너 or 멤버만 가능)
//    @PostMapping("/ai")
//    public ResponseEntity<Void> generate(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                         @RequestBody ScheduleGenerateRequest request) {
//
//         Long userId = userDetails.getUser().getId();
//         tripPermissionService.assertOwnerOrMember(request.getTripId(), userId);
//
//         // 프론트 요청을 그대로 Python으로 전달
//        AiScheduleRequest airequest = AiScheduleRequest.builder()
//                .places(request.getPlaces())
//                .n_days(request.getN_days())
//                .transportation_mode(request.getTransportation_mode())
//                .start_date(request.getStart_date())
//                .end_date(request.getEnd_date())
//                .daily_start_time(request.getDaily_start_time())
//                .daily_end_time(request.getDaily_end_time())
//                .user_preferences(request.getUser_preferences())
//                .pinned_places(request.getPinned_places())
//                .hotels(request.getHotels())
//                .departure_points(request.getDeparture_points())
//                .build();
//
//        scheduleService.generateAndSave(request.getTripId(), airequest);
//
//        return ResponseEntity.ok().build();
//    }
    @PostMapping("/ai")
    public ResponseEntity<List<ScheduleResponse>> generate(@RequestBody ScheduleGenerateRequest request) {
        AiScheduleRequest airequest = AiScheduleRequest.builder()
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

        List<ScheduleResponse> result = scheduleService.generateAndSave(request.getTripId(), airequest);
        return ResponseEntity.ok(result);
    }
    //    // 일정 조회 GET /api/schedules?tripId=1(Trip 오너 or 멤버만 가능))
//    @GetMapping
//    public ResponseEntity<List<ScheduleResponse>> getSchedules(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                                               @RequestParam Long tripId) {
//        Long userId = userDetails.getUser().getId();
//        tripPermissionService.assertOwnerOrMember(tripId, userId);
//
//        return ResponseEntity.ok(scheduleService.getSchedules(tripId));
//    }
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@RequestParam Long tripId) {
        return ResponseEntity.ok(scheduleService.getSchedules(tripId));
    }

}