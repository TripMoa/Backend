package com.tripmoa.story.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* 여행 후기 경비 요약 DTO
 - TRAVEL LOG 지출을 카테고리별로 합산한 값 (항목별 상세 내역은 담지 않음)
 - 혼자: 개인 + 공동 지출 / 여럿: 공동 지출만 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryExpenseSummary {

    private Integer transportation;  // 교통 (TRANS)
    private Integer accommodation;   // 숙소 (STAY)
    private Integer food;            // 식비 (FOOD)
    private Integer attraction;      // 관광/티켓 (TICKET)
    private Integer shopping;        // 쇼핑 (SHOP)
    private Integer etc;             // 기타 (ETC)
    private Integer total;           // 총 지출
    private Integer memberCount;     // 여행 인원
}
