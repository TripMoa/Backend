package com.tripmoa.trip.dto;

import com.tripmoa.trip.enums.TripVisibility;
import lombok.Getter;

@Getter
public class TripVisibilityUpdateRequest {

    private TripVisibility visibility;

}