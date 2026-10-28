package com.tripmoa.schedule.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 여행별 "AI 일정 생성 중" 상태
 *
 * 생성은 일정 전체(일차·노드)를 통째로 새로 만들어 덮어쓴다. 그 사이에 다른 멤버가 일정 구조를 고치면
 * 그 수정은 말없이 사라지므로, 생성 중에는 구조를 바꾸는 편집을 기다리지 않고 바로 거절한다.
 * (메모는 재생성이 그대로 유지하므로 막지 않는다)
 *
 * - 상태는 생성이 시작될 때 켜고 끝나면(성공·실패 모두) 반드시 끈다. 서버가 도중에 내려가면 상태도 함께 사라진다.
 * - JVM 메모리에만 둔다 — 서버가 한 대일 때만 유효하다 (TripScheduleLock 과 같은 제약).
 */
@Component
@RequiredArgsConstructor
public class ScheduleGenerationState {

    public record Info(long startedAt, Long actorUserId, String actorNickname) {
    }

    private final TripMemberRepository tripMemberRepository;
    private final ConcurrentHashMap<Long, Info> generating = new ConcurrentHashMap<>();

    /** 생성 시작 — 시작한 사람과 시각을 기록한다 */
    public void begin(Long tripId, Long actorUserId) {
        String nickname = actorUserId == null ? null
                : tripMemberRepository.findByTrip_IdAndUser_Id(tripId, actorUserId)
                        .map(TripMember::getNickname)
                        .orElse(null);
        generating.put(tripId, new Info(System.currentTimeMillis(), actorUserId, nickname));
    }

    /** 생성 종료 (성공·실패 모두) */
    public void end(Long tripId) {
        generating.remove(tripId);
    }

    public Optional<Info> infoOf(Long tripId) {
        return Optional.ofNullable(generating.get(tripId));
    }

    /** 생성 중이면 안내 문구와 함께 409로 거절한다. 일정 구조를 바꾸는 모든 쓰기 경로가 시작할 때 부른다. */
    public void assertNotGenerating(Long tripId) {
        Info info = generating.get(tripId);
        if (info == null) return;

        String who = info.actorNickname() != null ? info.actorNickname() + "님이" : "다른 멤버가";
        throw new BusinessException(ErrorCode.CONFLICT,
                "지금 " + who + " AI 일정을 만드는 중이에요. 끝나면 다시 시도해주세요.");
    }
}
