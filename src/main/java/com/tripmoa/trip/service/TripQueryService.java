package com.tripmoa.trip.service;

import com.tripmoa.trip.dto.MyTripSummaryResponse;
import com.tripmoa.trip.dto.TripDetailResponse;
import com.tripmoa.trip.dto.TripMemberResponse;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripQueryService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPermissionService tripPermissionService;

    // 내가 소유한 여행 조회
    public List<MyTripSummaryResponse> getMyTrips(Long userId, TripVisibility visibility, TripStatus tripStatus) {
        List<Trip> trips = (visibility == null)
                ? tripRepository.findAllByOwner_IdAndStatusOrderByCreatedAtDesc(userId, tripStatus)
                : tripRepository.findAllByOwner_IdAndVisibilityAndStatusOrderByCreatedAtDesc(userId, visibility, tripStatus);

        return toMyTripSummaryResponses(trips);
    }

    // 내가 멤버인 여행 조회
    public List<MyTripSummaryResponse> getInvitedTrips(Long userId, TripStatus tripStatus) {
        List<Trip> trips = tripRepository.findAllInvitedTripsByUserIdAndStatus(userId, tripStatus);
        return toMyTripSummaryResponses(trips);
    }

    // 여행 목록 + 멤버 목록을 한 번에 DTO로 변환
    private List<MyTripSummaryResponse> toMyTripSummaryResponses(List<Trip> trips) {
        if (trips.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tripIds = trips.stream()
                .map(Trip::getId)
                .toList();

        Map<Long, List<TripMemberResponse>> memberMap = tripMemberRepository
                .findAllByTrip_IdInOrderByTrip_IdAscSortOrderAsc(tripIds)
                .stream()
                .collect(Collectors.groupingBy(
                        member -> member.getTrip().getId(),
                        Collectors.mapping(TripMemberResponse::from, Collectors.toList())
                ));

        return trips.stream()
                .map(trip -> MyTripSummaryResponse.from(
                        trip,
                        memberMap.getOrDefault(trip.getId(), Collections.emptyList())
                ))
                .toList();
    }

    // 여행 상세 조회
    public TripDetailResponse getTripDetail(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Trip trip = tripPermissionService.getTripOr404(tripId);
        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);

        return TripDetailResponse.from(trip, members);
    }

    // 여행 멤버 조회
    public List<TripMemberResponse> getTripMembers(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        return tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId).stream()
                .map(TripMemberResponse::from)
                .toList();
    }
}