package com.tripmoa.place.dto;

import com.tripmoa.place.domain.Place;
import lombok.Builder;
import lombok.Getter;

/**
 * Place 응답 DTO
 *
 * - 백엔드 → 프론트 반환용
 */
@Getter
@Builder
public class PlaceResponse {

    private Long id;
    private Long tripId;

    private String name;
    private String category;

    private Double lat;
    private Double lng;

    private String address;
    private String description;

    //Entity -> Dto 변환
    public static PlaceResponse from(Place place) {
        return PlaceResponse.builder()
                .id(place.getId())
                .tripId(place.getTripId())
                .name(place.getName())
                .category(place.getCategory())
                .lat(place.getLat())
                .lng(place.getLng())
                .address(place.getAddress())
                .description(place.getDescription())
                .build();
    }
}
