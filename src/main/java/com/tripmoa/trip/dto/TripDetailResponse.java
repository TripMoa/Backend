package com.tripmoa.trip.dto;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TripDetailResponse {
    private Long tripId;
    private String title;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private String status;
    private String visibility;
    private String inviteCode;

    private Long ownerUserId;
    private String ownerName;

    private List<TripMemberResponse> members;

    public static TripDetailResponse from(Trip trip, List<TripMember> members) {
        return TripDetailResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .tripStartDate(trip.getTripStartDate())
                .tripEndDate(trip.getTripEndDate())
                .status(trip.getStatus().name())
                .visibility(trip.getVisibility().name())
                .inviteCode(trip.getInviteCode())
                .ownerUserId(trip.getOwner().getId())
                .ownerName(trip.getOwner().getName())
                .members(members.stream()
                        .map(TripMemberResponse::from)
                        .toList())
                .build();
    }
}
