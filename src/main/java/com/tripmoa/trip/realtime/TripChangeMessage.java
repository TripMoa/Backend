package com.tripmoa.trip.realtime;

import java.util.List;

/**
 * 여행 실시간 알림 (/sub/trip/{tripId} 로 나가는 메시지)
 *
 * 변경된 데이터 자체는 싣지 않는다 — "무엇이 바뀌었다"는 신호만 보내고, 받은 쪽이 REST로 다시 조회한다.
 *
 * @param domain      "SCHEDULE" | "PLACE"
 * @param action      TripChangeAction 이름
 * @param scheduleIds 바뀐 일차(Schedule) id들. 비어 있으면 "전체를 다시 불러와야 한다"는 뜻 (SCHEDULE 도메인)
 * @param itemTitle   바뀐 노드/장소 이름 (알 수 있을 때만, 토스트 문구용)
 * @param actorUserId 변경한 사용자
 * @param actorNickname 변경한 사용자의 이 여행 닉네임
 * @param originId    변경을 일으킨 요청의 X-Client-Id 헤더 값 — 받은 쪽이 "내가 방금 한 변경"을 걸러내는 데 쓴다 (없으면 null)
 * @param at          발행 시각 (epoch millis)
 */
public record TripChangeMessage(
        String domain,
        String action,
        List<Long> scheduleIds,
        String itemTitle,
        Long actorUserId,
        String actorNickname,
        String originId,
        long at
) {
}
