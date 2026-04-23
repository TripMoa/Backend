package com.tripmoa.place.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Python 검색 API 응답 → 프론트 전달용 DTO
 */
@Getter
@NoArgsConstructor
public class PlaceSearchResponse {

    private boolean success;
    private int total;
    private int display;
    private List<PlaceSearchItem> places;
    private String query;
    private String method;

    @Getter
    @NoArgsConstructor
    public static class PlaceSearchItem {
        private String name;
        private String address;
        private Double lat;
        private Double lng;
        private String category;
        private String description;
        private String telephone;
        private String link;
    }

}
