package com.tripmoa.schedule.dto;

import lombok.Getter;

@Getter
public class ScheduleItemCreateRequest {
    private Long scheduleId;   // 어떤 day에 추가할지
    private String time;
    private String title;
    private String description;
    private String category;   // 백엔드 카테고리명(관광지/맛집/카페/쇼핑/숙소/출발지) — 장소 검색으로 추가할 때만
    private Double lat;
    private Double lng;
}