package com.tripmoa.schedule.dto;

import lombok.Getter;

/**
 * 노드의 장소 바꾸기 — 순서·머무는 시간·고정 시각은 그대로 두고 장소(이름·주소·카테고리·좌표)만 바꾼다
 */
@Getter
public class ScheduleItemPlaceRequest {
    private String title;
    private String description;   // 주소 등
    private String category;      // 백엔드 카테고리명(관광지/맛집/카페/쇼핑)
    private Double lat;
    private Double lng;
}
