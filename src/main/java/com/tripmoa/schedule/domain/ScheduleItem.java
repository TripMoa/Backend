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

    // 머무는 시간(분) — 자동 계산의 기준. 비어 있으면(예전 일정) 엔진의 카테고리·이름 기본값을 쓴다
    private Integer stayMinutes;

    // 고정 시각 ("HH:MM") — 있으면 자동 계산이 이 시각에 시작하도록 맞춘다(비어 있으면 고정 안 함)
    private String pinnedTime;

    // 멤버가 함께 보는 메모 (예약번호, 먹을 메뉴 등) — 일정 계산과 무관
    @Column(length = 1000)
    private String memo;

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
    /**
     * AI 재생성 때 같은 장소의 기존 노드를 지우지 않고 재사용하면서 새 결과로 덮어쓴다.
     * - 유지: id(그래서 지출·바우처 연결이 그대로), 멤버 메모
     * - 덮어씀: 일차·순서·시각·머무는 시간·고정 시각·이동 정보 등 생성 결과 전부
     * ※ 노드에 필드를 추가하면 "재생성 때 덮어쓸지 유지할지"를 여기서 정한다.
     */
    public void refreshFrom(ScheduleItem fresh) {
        this.scheduleId = fresh.scheduleId;
        this.orderIndex = fresh.orderIndex;
        this.time = fresh.time;
        this.title = fresh.title;
        this.category = fresh.category;
        this.description = fresh.description;
        this.lat = fresh.lat;
        this.lng = fresh.lng;
        this.stayMinutes = fresh.stayMinutes;
        this.pinnedTime = fresh.pinnedTime;
        this.travelMinutes = fresh.travelMinutes;
        this.travelPayment = fresh.travelPayment;
        this.travelTransfer = fresh.travelTransfer;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    // 장소 바꾸기 — 이동 정보(다음 장소까지)는 새 장소 기준이 아니므로 비운다(자동 계산이 켜진 날은 곧 다시 채워진다)
    public void replacePlace(String title, String description, String category, Double lat, Double lng) {
        this.title = title;
        this.description = description != null ? description : "";
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        clearTravel();
    }

    public void clearTravel() {
        this.travelMinutes = null;
        this.travelPayment = null;
        this.travelTransfer = null;
    }

    // 계획 변경 — stayGiven/pinGiven이 true인 값만 바꾼다(pinnedTime이 null이면 고정 해제)
    public void updatePlan(boolean stayGiven, Integer stayMinutes, boolean pinGiven, String pinnedTime) {
        if (stayGiven) this.stayMinutes = stayMinutes;
        if (pinGiven) this.pinnedTime = pinnedTime;
    }

    // 자동 계산 결과 반영 (요금·환승은 추정 계산이라 비운다)
    public void applyComputed(String time, Integer stayMinutes, Integer travelMinutes) {
        this.time = time;
        this.stayMinutes = stayMinutes;
        this.travelMinutes = travelMinutes;
        this.travelPayment = null;
        this.travelTransfer = null;
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