package com.tripmoa.trip.realtime;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.global.websocket.StompDestinationPolicy;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 여행 실시간 구독 규칙: /sub/trip/{tripId} 는 그 여행의 소유자·멤버만 구독할 수 있다.
 *
 * - 주소는 정확히 "/sub/trip/" + 숫자 id 형태만 허용한다 (와일드카드·"05"·"+5" 같은 변형은 거부).
 * - 없는 여행·멤버가 아닌 여행은 이유를 구분하지 않고 같은 FORBIDDEN 으로 거부한다 (여행 존재 여부 노출 방지).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TripDestinationPolicy implements StompDestinationPolicy {

    private final TripPermissionService tripPermissionService;

    @Override
    public boolean supports(String destination) {
        return destination != null && destination.startsWith(TripRealtimePublisher.TOPIC_PREFIX);
    }

    @Override
    public void authorizeSubscribe(Long userId, String destination) {
        Long tripId;
        try {
            tripId = Long.parseLong(destination.substring(TripRealtimePublisher.TOPIC_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // "05", "+5" 처럼 같은 숫자의 다른 표기는 실제 발행 주소와 달라 쓸모가 없으므로 정확한 형태만 통과시킨다
        if (!destination.equals(TripRealtimePublisher.topic(tripId))) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        try {
            tripPermissionService.assertOwnerOrMember(tripId, userId);
        } catch (BusinessException e) {
            log.warn("여행 구독 거부 userId={}, tripId={}", userId, tripId);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
