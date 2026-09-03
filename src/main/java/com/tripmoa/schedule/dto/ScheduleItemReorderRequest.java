package com.tripmoa.schedule.dto;

import lombok.Getter;
import java.util.List;

/**
 * 순서 변경 요청
 * itemIds: 새로운 순서대로 정렬된 ScheduleItem id 목록
 */
@Getter
public class ScheduleItemReorderRequest {
    private List<Long> itemIds;
}