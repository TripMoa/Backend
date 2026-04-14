package com.tripmoa.notice.repository;

import com.tripmoa.notice.entity.NoticeTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeTagRepository extends JpaRepository<NoticeTag, Long> {

    /**
     * 특정 여행의 최근 사용 태그 조회
     * - 생성일 최신순
     */
    @EntityGraph(attributePaths = {"trip"})
    List<NoticeTag> findTop10ByTrip_IdOrderByCreatedAtDesc(Long tripId);

    /**
     * 특정 여행 내 태그명 존재 여부 확인
     */
    boolean existsByTrip_IdAndName(Long tripId, String name);

    /**
     * 특정 여행 + 태그명 단건 조회
     */
    @EntityGraph(attributePaths = {"trip"})
    Optional<NoticeTag> findByTrip_IdAndName(Long tripId, String name);

    /**
     * 특정 여행의 전체 태그 목록 조회
     */
    @EntityGraph(attributePaths = {"trip"})
    List<NoticeTag> findByTrip_IdOrderByCreatedAtDesc(Long tripId);
}