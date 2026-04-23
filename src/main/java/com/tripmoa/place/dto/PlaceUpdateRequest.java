package com.tripmoa.place.dto;

import lombok.Getter;

/**
 * 장소 수정 요청 DTO (카테고리, 메모만 수정 가능)
 */
@Getter
public class PlaceUpdateRequest {
    private String category;
    private String memo;
}