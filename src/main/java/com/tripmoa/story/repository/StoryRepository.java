package com.tripmoa.story.repository;

import com.tripmoa.story.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.tripmoa.story.enums.StoryType;

/* 여행기 Repository
 - Story 엔티티의 DB 접근 담당 */

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // 특정 사용자가 작성한 여행기 목록 조회 (최신순)
    List<Story> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    // 전체 조회 (최신순)
    List<Story> findAllByOrderByCreatedAtDesc();

    // 태그 포함된 게시글 조회
    List<Story> findByTagsContaining(String tag);

    // 특정 여행(tripId)에 대해 해당 작성자(authorId)의 리뷰가 이미 존재하는지 확인
    boolean existsByTrip_IdAndAuthor_Id(Long tripId, Long authorId);

    // 공개 글만 조회 (피드용) - REVIEW는 공개만, FREE는 다 보임
    List<Story> findByIsPublicTrueOrTypeOrderByCreatedAtDesc(StoryType type);

    // 태그 + 조건
    List<Story> findByTagsContainingAndIsPublicTrueOrTagsContainingAndTypeOrderByCreatedAtDesc(String tag1, String tag2, StoryType type);
}