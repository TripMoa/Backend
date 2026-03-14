package com.tripmoa.blog.repository;

import com.tripmoa.blog.domain.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/* 드래프트 Repository
 - Draft 엔티티의 DB 접근 담당 */

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {

    // 특정 사용자의 드래프트 목록 조회 (최근 수정 순)
    List<Draft> findByAuthorIdOrderByUpdatedAtDesc(Long authorId);
}