package com.tripmoa.trip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TripMemberAddRequest {

    @NotNull
    private Long userId;

}
