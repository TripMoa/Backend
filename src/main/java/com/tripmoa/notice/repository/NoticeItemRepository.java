package com.tripmoa.notice.repository;

import com.tripmoa.notice.entity.NoticeItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeItemRepository extends JpaRepository<NoticeItem, Long> {

    /**
     * 특정 여행 + 특정 그룹의 공지 메모 전체 조회
     * - 고정된 메모 우선, 이후 최신순
     */
    @EntityGraph(attributePaths = {"noticeGroup", "noticeGroup.trip", "createdByUser", "updatedByUser"})
    List<NoticeItem> findByNoticeGroup_Trip_IdAndNoticeGroup_IdOrderByIsPinnedDescCreatedAtDesc(
            Long tripId,
            Long noticeGroupId
    );

    /**
     * 특정 여행의 공지 메모 단건 조회
     */
    @EntityGraph(attributePaths = {"noticeGroup", "noticeGroup.trip", "createdByUser", "updatedByUser"})
    Optional<NoticeItem> findByIdAndNoticeGroup_Trip_Id(Long noticeItemId, Long tripId);

    /**
     * 특정 그룹에 속한 공지 메모 개수
     * - 그룹 삭제 가능 여부 판단 등에 사용 가능
     */
    long countByNoticeGroup_Id(Long noticeGroupId);
}