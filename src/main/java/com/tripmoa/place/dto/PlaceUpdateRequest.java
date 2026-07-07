package com.tripmoa.place.dto;

import lombok.Getter;

/**
 * 장소 수정 요청 DTO (카테고리만 수정 가능)
 */
@Getter
public class PlaceUpdateRequest {
    private String category;
}