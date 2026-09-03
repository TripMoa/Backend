package com.tripmoa.place.controller;

import com.tripmoa.place.domain.Place;
import com.tripmoa.place.dto.PlaceCreateRequest;
import com.tripmoa.place.dto.PlaceResponse;
import com.tripmoa.place.dto.PlaceUpdateRequest;
import com.tripmoa.place.service.PlaceService;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PlaceController
 *
 * - 장소 API
 */
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final TripPermissionService tripPermissionService;

    //장소 저장 POST /api/places
    @PostMapping
    public ResponseEntity<PlaceResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestBody PlaceCreateRequest request) {

        Long userId = userDetails.getUser().getId();
        tripPermissionService.assertOwnerOrMember(request.getTripId(), userId);

        return ResponseEntity.ok(placeService.save(request));
    }

    // 장소 조회 GET /api/places?tripId=1
    @GetMapping
    public ResponseEntity<List<PlaceResponse>> get(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestParam Long tripId) {

        Long userId = userDetails.getUser().getId();
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        return ResponseEntity.ok(placeService.getPlaces(tripId));
    }

    // 카테고리 / 메모 수정
    @PatchMapping("/{placeId}")
    public ResponseEntity<PlaceResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId,
            @RequestBody PlaceUpdateRequest request
    ) {
        // placeId로 장소를 먼저 조회해서 tripId를 얻은 뒤 권한 체크 (PlaceService 내부에서 처리)
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(placeService.update(placeId, userId, request));
    }

    // 장소 삭제 DELETE /api/places/{placeId}
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long placeId) {

        // placeId로 장소를 먼저 조회해서 tripId를 얻은 뒤 권한 체크 (PlaceService 내부에서 처리)
        Long userId = userDetails.getUser().getId();
        placeService.deletePlace(placeId, userId);

        return ResponseEntity.noContent().build();
    }

}
