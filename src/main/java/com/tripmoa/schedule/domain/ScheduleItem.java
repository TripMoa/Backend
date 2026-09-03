package com.tripmoa.schedule.domain;


import jakarta.persistence.*;
import lombok.*;

/**
 * ScheduleItem (타임라인 단위)
 *
 * - 실제 일정 하나
 * - 예: 09:00 경복궁 방문
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 Day(Schedule)에 속하는지
    private Long scheduleId;

    // 시간 (09:00)
    private String time;

    // 제목 (경복궁)
    private String title;

    // 카테고리 (관광지, 맛집 등)
    private String category;

    // 상세 설명
    @Column(length = 1000)
    private String description;

    // 순서 (정렬용)
    private int orderIndex;

    // 좌표 (지도 표시용)
    private Double lat;
    private Double lng;

    // 다음 장소까지 이동시간 (분) - ODsay 실측값 또는 하버사인 추정치
    private Integer travelMinutes;

    // 다음 장소까지 대중교통 요금 (원) - ODsay 실측값이 있을 때만 존재
    private Integer travelPayment;

    // 다음 장소까지 환승 횟수 - ODsay 실측값이 있을 때만 존재
    private Integer travelTransfer;

    // 노드 수정
    public void update(String time, String title, String description) {
        if (time != null) this.time = time;
        if (title != null) this.title = title;
        if (description != null) this.description = description;
    }
    // 순서 변경
    public void updateOrder(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    // 다른 날로 이동
    public void move(Long targetScheduleId, int orderIndex) {
        this.scheduleId = targetScheduleId;
        this.orderIndex = orderIndex;
    }
}