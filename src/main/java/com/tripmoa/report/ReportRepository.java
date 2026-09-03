package com.tripmoa.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<UserReport, Long> {

    // 같은 사람이 같은 대상을 이미 신고했는지 (중복 신고 방지)
    boolean existsByReporterIdAndLocationAndTargetId(
            Long reporterId,
            ReportLocation location,
            Long targetId
    );

    // 페이징 신고 내역 조회
    Page<UserReport> findAllByReportedUserIdOrderByReportedAtDesc(
            Long reportedUserId,
            Pageable pageable
    );

    // 특정 사용자가 신고당한 전체 내역 최신순 조회
    List<UserReport> findAllByReportedUserIdOrderByReportedAtDesc(Long reportedUserId);

    // 특정 사용자가 신고당한 총 횟수 조회
    long countByReportedUserId(Long reportedUserId);

    // 특정 위치의 특정 대상이 신고당한 횟수 조회
    long countByLocationAndTargetId(ReportLocation location, Long targetId);

    // 로그인한 사용자가 직접 신고한 대상 ID 목록 조회
    @Query("""
            select distinct r.targetId
            from UserReport r
            where r.reporter.id = :reporterId
              and r.location = :location
            order by r.targetId desc
            """)
    List<Long> findDistinctTargetIdsByReporterIdAndLocation(Long reporterId, ReportLocation location);

}