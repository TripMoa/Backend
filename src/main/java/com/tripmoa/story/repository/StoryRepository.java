package com.tripmoa.story.repository;

import com.tripmoa.story.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/* 여행기 Repository
 - Story 엔티티의 DB 접근 담당 */

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // 특정 사용자가 작성한 여행기 목록 조회 (최신순)
    List<Story> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    // 태그 포함된 게시글 조회
    List<Story> findByTagsContaining(String tag);

}