package com.tripmoa.schedule.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 여행(trip) 단위 일정 변경 잠금
 *
 * AI 일정 생성과 일차(Day) 행 만들기는 같은 여행의 일차 행을 만들고 지운다. 동시에 실행되면
 * 같은 일차 행이 중복으로 생기므로(DB에 (tripId, day) 유일 제약이 없다) 여행별로 한 번에 하나만 실행한다.
 *
 * 이 백엔드는 서버 한 대(컨테이너 하나)로 운영되어 JVM 안의 잠금으로 충분하다.
 * 서버를 여러 대로 늘리면 Redis 같은 분산 잠금으로 바꿔야 한다.
 * (잠금 객체는 여행 id마다 하나씩 만들어 그대로 두는데, 여행 수만큼의 작은 객체라 무시할 만하다)
 */
@Component
public class TripScheduleLock {

    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    private ReentrantLock lockOf(Long tripId) {
        return locks.computeIfAbsent(tripId, id -> new ReentrantLock());
    }

    /** 기다리지 않고 시도 — 이미 다른 요청이 잡고 있으면 false */
    public boolean tryLock(Long tripId) {
        return lockOf(tripId).tryLock();
    }

    /** 최대 timeout만큼 기다려서 시도 */
    public boolean tryLock(Long tripId, long timeout, TimeUnit unit) {
        try {
            return lockOf(tripId).tryLock(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** 잠금을 잡은 스레드만 풀 수 있다 */
    public void unlock(Long tripId) {
        ReentrantLock lock = locks.get(tripId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
