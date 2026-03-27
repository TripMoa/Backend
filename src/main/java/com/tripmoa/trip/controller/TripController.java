package com.tripmoa.trip.controller;

import com.tripmoa.security.princpal.CustomUserDetails;
import com.tripmoa.trip.dto.*;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import com.tripmoa.trip.service.TripCommandService;
import com.tripmoa.trip.service.TripQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trip", description = "내 여행 API")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripQueryService tripQueryService;
    private final TripCommandService tripCommandService;

    // 내가 소유한 여행 목록 조회
    // 전체: /api/trips/my
    // 공개: /api/trips/my?visibility=PUBLIC
    // 비공개: /api/trips/my?visibility=PRIVATE
    @GetMapping("/my")
    public ResponseEntity<List<MyTripSummaryResponse>> getMyTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) TripVisibility visibility
    ) {
        Long userId = userDetails.getUser().getId();

        return ResponseEntity.ok(tripQueryService.getMyTrips(userId, visibility, TripStatus.ACTIVE));
    }

    // 내가 멤버로 참여한 여행 목록 조회
    @GetMapping("/invited")
    public ResponseEntity<List<MyTripSummaryResponse>> getInvitedTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(tripQueryService.getInvitedTrips(userId, TripStatus.ACTIVE));
    }

    // 여행 생성
    @PostMapping
    public ResponseEntity<TripDetailResponse> createTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TripCreateRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(tripCommandService.createTrip(userId, request));
    }

    // 여행 상세 조회
    @GetMapping("/{tripId}")
    public ResponseEntity<TripDetailResponse> getTripDetail(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(tripQueryService.getTripDetail(tripId, userId));
    }

    // 여행 멤버 조회
    @GetMapping("/{tripId}/members")
    public ResponseEntity<List<TripMemberResponse>> getTripMembers(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(tripQueryService.getTripMembers(tripId, userId));
    }

    // 여행 기본 정보 수정
    @PatchMapping("/{tripId}")
    public ResponseEntity<TripDetailResponse> updateTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TripUpdateRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(tripCommandService.updateTrip(tripId, userId, request));
    }

    // 여행 공개 여부 수정
    @PatchMapping("/{tripId}/visibility")
    public ResponseEntity<TripDetailResponse> updateTripVisibility(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TripVisibilityUpdateRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(
                tripCommandService.updateTripVisibility(tripId, userId, request.getVisibility())
        );
    }

    // 여행 멤버 닉네임 수정 (본인만 가능)
    @PatchMapping("/{tripId}/members/{memberId}/nickname")
    public ResponseEntity<TripMemberResponse> updateTripMemberNickname(
            @PathVariable Long tripId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TripMemberNicknameUpdateRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(
                tripCommandService.updateTripMemberNickname(tripId, memberId, userId, request)
        );
    }

    // 여행 삭제 (소프트 삭제: ARCHIVED 처리)
    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        tripCommandService.deleteTrip(tripId, userId);
        return ResponseEntity.noContent().build();
    }
}