package com.tripmoa.place.dto;

import lombok.Getter;

/**
 * Place 생성 요청 DTO
 *
 * - 프론트 → 백엔드 요청용
 * - entity 직접 받지 않고 DTO로 받는 게 정석
 */
@Getter
public class PlaceCreateRequest {

    private Long tripId;

    private String name;
    private String category;

    private Double lat;
    private Double lng;

    private String address;
    private String description;
    private String memo;
}