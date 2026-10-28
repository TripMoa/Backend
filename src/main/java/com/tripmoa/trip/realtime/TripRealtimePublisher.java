package com.tripmoa.trip.realtime;

import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 여행 멤버들에게 "일정/장소가 바뀌었다"는 실시간 알림을 보낸다 (주소: /sub/trip/{tripId}).
 *
 * - 변경한 사람과 요청 헤더(X-Client-Id)는 현재 요청에서 스스로 읽는다. 호출하는 쪽은 tripId·동작·바뀐 일차만 넘기면 된다.
 * - 알림은 저장(과 자동 재계산)이 끝난 뒤에 나가야 하므로, 열린 트랜잭션 안에서 불리면 커밋 뒤로 미룬다.
 * - 알림 실패가 사용자의 편집을 실패시키면 안 되므로 예외는 삼키고 로그만 남긴다.
 * - 메모리 브로커라서 백엔드가 한 대일 때만 모든 멤버에게 닿는다 (여러 대로 늘리면 외부 브로커가 필요).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TripRealtimePublisher {

    public static final String TOPIC_PREFIX = "/sub/trip/";
    private static final String ORIGIN_HEADER = "X-Client-Id";
    private static final int MAX_ORIGIN_LENGTH = 64;

    private final SimpMessagingTemplate messagingTemplate;
    private final TripMemberRepository tripMemberRepository;

    public static String topic(Long tripId) {
        return TOPIC_PREFIX + tripId;
    }

    /** 일정이 바뀜. scheduleIds 가 비어 있으면 받은 쪽이 전체를 다시 불러온다. */
    public void schedulesChanged(Long tripId, TripChangeAction action, List<Long> scheduleIds, String itemTitle) {
        send(tripId, "SCHEDULE", action, scheduleIds, itemTitle);
    }

    /** 저장된 장소가 바뀜 */
    public void placesChanged(Long tripId, TripChangeAction action, String placeName) {
        send(tripId, "PLACE", action, List.of(), placeName);
    }

    private void send(Long tripId, String domain, TripChangeAction action, List<Long> scheduleIds, String itemTitle) {
        if (tripId == null) return;
        try {
            Long actorUserId = currentUserId();
            TripChangeMessage message = new TripChangeMessage(
                    domain,
                    action.name(),
                    scheduleIds == null ? List.of() : List.copyOf(scheduleIds),
                    itemTitle,
                    actorUserId,
                    nicknameOf(tripId, actorUserId),
                    currentOriginId(),
                    System.currentTimeMillis()
            );

            Runnable push = () -> {
                try {
                    messagingTemplate.convertAndSend(topic(tripId), message);
                } catch (Exception e) {
                    log.warn("여행 실시간 알림 전송 실패 tripId={}, action={}: {}", tripId, action, e.getMessage());
                }
            };

            if (TransactionSynchronizationManager.isSynchronizationActive()
                    && TransactionSynchronizationManager.isActualTransactionActive()) {
                // 아직 커밋 전이면 받은 쪽이 다시 조회해도 옛 값을 보게 되므로 커밋 뒤에 보낸다
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        push.run();
                    }
                });
            } else {
                push.run();
            }
        } catch (Exception e) {
            log.warn("여행 실시간 알림 준비 실패 tripId={}, action={}: {}", tripId, action, e.getMessage());
        }
    }

    private String nicknameOf(Long tripId, Long userId) {
        if (userId == null) return null;
        return tripMemberRepository.findByTrip_IdAndUser_Id(tripId, userId)
                .map(TripMember::getNickname)
                .orElse(null);
    }

    private static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getId();
        }
        return null;
    }

    private static String currentOriginId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String value = attrs.getRequest().getHeader(ORIGIN_HEADER);
            if (value != null && !value.isBlank() && value.length() <= MAX_ORIGIN_LENGTH) {
                return value;
            }
        }
        return null;
    }
}
