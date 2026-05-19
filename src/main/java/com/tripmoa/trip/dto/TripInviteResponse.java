package com.tripmoa.trip.dto;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TripInviteResponse {

    private Long tripId;
    private String title;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private String ownerNickname;
    private int memberCount;
    private List<TripMemberResponse> members;

    public static TripInviteResponse from(Trip trip, List<TripMember> members) {
        return TripInviteResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .tripStartDate(trip.getTripStartDate())
                .tripEndDate(trip.getTripEndDate())
                .ownerNickname(trip.getOwner().getNickname())
                .memberCount(members.size())
                .members(members.stream()
                        .map(TripMemberResponse::from)
                        .toList())
                .build();
    }
}