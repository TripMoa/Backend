package com.tripmoa.schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 재생성 "되돌리기"용 스냅샷 — 여행마다 가장 최근 재생성 직전의 일정 하나만 보관한다.
 *
 * - payload: 재생성 직전의 일차·노드 전체(JSON)
 * - afterFingerprint: 재생성이 끝난 직후 일정의 지문. 되돌릴 때 지금 일정의 지문과 같아야(= 그 뒤로 아무도 안 고쳤어야) 한다.
 *   지문은 재생성 트랜잭션 끝에서 채워진다 — 채워지기 전(null)에는 되돌릴 수 없다.
 * - 되돌리거나 새로 재생성하면 이 행은 지워지거나 새 내용으로 바뀐다.
 */
@Entity
@Table(name = "schedule_snapshot", uniqueConstraints = @UniqueConstraint(columnNames = "trip_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    // @Lob 만 쓰면 MySQL 에서 작은 타입으로 만들어져 일정이 길면 저장이 실패한다 — 크기를 직접 정한다
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(length = 64)
    private String afterFingerprint;

    // 재생성한 사람과 시각 — "누가 만든 새 일정을 되돌리는지" 보여주는 데 쓴다
    private Long createdByUserId;
    private String createdByNickname;
    private LocalDateTime createdAt;

    public ScheduleSnapshot(Long tripId, String payload, Long createdByUserId, String createdByNickname) {
        this.tripId = tripId;
        this.payload = payload;
        this.createdByUserId = createdByUserId;
        this.createdByNickname = createdByNickname;
        this.createdAt = LocalDateTime.now();
    }

    public void sealWith(String fingerprint) {
        this.afterFingerprint = fingerprint;
    }
}
