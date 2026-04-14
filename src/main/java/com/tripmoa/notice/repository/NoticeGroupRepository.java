package com.tripmoa.notice.repository;

import com.tripmoa.notice.entity.NoticeGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeGroupRepository extends JpaRepository<NoticeGroup, Long> {

    /**
     * 특정 여행의 공지 그룹 전체 조회
     * - 정렬 순서 오름차순, 생성일 오름차순
     */
    @EntityGraph(attributePaths = {"trip"})
    List<NoticeGroup> findByTrip_IdOrderBySortOrderAscCreatedAtAsc(Long tripId);

    /**
     * 특정 여행의 공지 그룹 단건 조회
     */
    @EntityGraph(attributePaths = {"trip"})
    Optional<NoticeGroup> findByIdAndTrip_Id(Long groupId, Long tripId);

    /**
     * 특정 여행 내 동일한 그룹명 존재 여부 확인
     */
    boolean existsByTrip_IdAndName(Long tripId, String name);

    /**
     * 특정 여행 내 동일한 그룹명 존재 여부 확인
     * - 수정 시 자기 자신 제외
     */
    boolean existsByTrip_IdAndNameAndIdNot(Long tripId, String name, Long groupId);

    /**
     * 특정 여행의 기본 그룹 조회
     */
    @EntityGraph(attributePaths = {"trip"})
    Optional<NoticeGroup> findByTrip_IdAndIsDefaultTrue(Long tripId);
}