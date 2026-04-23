package com.tripmoa.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<UserReport, Long> {

    // 같은 사람이 같은 대상을 이미 신고했는지 (중복 신고 방지)
    boolean existsByReporterIdAndLocationAndTargetId(Long reporterId, ReportLocation location, Long targetId);
}