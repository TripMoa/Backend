package com.tripmoa.trip.dto;

import com.tripmoa.trip.entity.Trip;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MyTripSummaryResponse {
    private Long tripId;
    private String title;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private String status;
    private String visibility;

    private Long ownerUserId;
    private String ownerName;

    private List<TripMemberResponse> members;

    public static MyTripSummaryResponse from(Trip trip, List<TripMemberResponse> members) {
        return MyTripSummaryResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .tripStartDate(trip.getTripStartDate())
                .tripEndDate(trip.getTripEndDate())
                .status(trip.getStatus().name())
                .visibility(trip.getVisibility().name())
                .ownerUserId(trip.getOwner().getId())
                .ownerName(trip.getOwner().getName())
                .members(members)
                .build();
    }
}