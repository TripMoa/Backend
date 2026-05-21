package com.tripmoa.trip.controller;

import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.trip.dto.TripDetailResponse;
import com.tripmoa.trip.dto.TripInviteResponse;
import com.tripmoa.trip.service.TripInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/invites")
@RequiredArgsConstructor
public class TripInviteController {

    private final TripInviteService tripInviteService;

    @GetMapping("/{inviteCode}")
    public ResponseEntity<TripInviteResponse> getInviteInfo(
            @PathVariable String inviteCode
    ) {
        return ResponseEntity.ok(tripInviteService.getInviteInfo(inviteCode));
    }

    @PostMapping("/{inviteCode}/join")
    public ResponseEntity<TripDetailResponse> joinTrip(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(tripInviteService.joinTrip(inviteCode, userId));
    }
}
