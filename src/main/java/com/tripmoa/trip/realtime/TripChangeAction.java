package com.tripmoa.trip.realtime;

/**
 * 여행에서 "무엇이 바뀌었는지" (실시간 알림의 action 값)
 */
public enum TripChangeAction {

    // 일정 노드
    ITEM_ADDED,
    ITEM_UPDATED,
    ITEM_DELETED,
    ITEM_MOVED,
    ITEMS_REORDERED,
    ITEM_PLAN_CHANGED,
    ITEM_MEMO_CHANGED,
    ITEM_PLACE_REPLACED,

    // 일정 일차 단위
    DAY_CREATED,
    DAY_SETTINGS_CHANGED,
    DAY_AUTO_COMPUTE_CHANGED,
    DAY_RECOMPUTED,

    // AI 생성: 시작되면 다른 멤버의 구조 편집이 막힌다. 끝은 성공(SCHEDULE_GENERATED — 일정 전체가 교체됨) 또는 실패(GENERATE_FAILED — 일정은 그대로)
    GENERATE_START,
    SCHEDULE_GENERATED,
    GENERATE_FAILED,

    // AI 재생성을 직전 상태로 되돌림 — 일정 전체가 교체되므로 SCHEDULE_GENERATED 처럼 받은 쪽이 전체를 다시 불러온다
    SCHEDULE_RESTORED,

    // 저장된 장소
    PLACE_ADDED,
    PLACE_UPDATED,
    PLACE_DELETED
}
