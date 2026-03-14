package com.tripmoa.blog.repository;

import com.tripmoa.blog.domain.SavedItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* 저장된 일정 Repository
 - SavedItinerary 엔티티의 DB 접근 담당 */

@Repository
public interface SavedItineraryRepository extends JpaRepository<SavedItinerary, Long> {

    // 특정 게시글을 사용자가 저장했는지 확인
    boolean existsByBlogIdAndUserId(Long blogId, Long userId);

    // 특정 게시글의 저장된 일정 조회
    Optional<SavedItinerary> findByBlogIdAndUserId(Long blogId, Long userId);

    // 특정 사용자가 저장한 일정 목록 조회
    List<SavedItinerary> findByUserId(Long userId);

    // 저장된 일정 삭제
    void deleteByBlogIdAndUserId(Long blogId, Long userId);
}