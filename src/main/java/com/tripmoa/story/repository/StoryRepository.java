package com.tripmoa.story.repository;

import com.tripmoa.story.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* 여행기 Repository
 - Story 엔티티의 DB 접근 담당 */

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
}