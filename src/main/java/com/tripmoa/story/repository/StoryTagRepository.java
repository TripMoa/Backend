package com.tripmoa.story.repository;

import com.tripmoa.story.domain.StoryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/* 태그 Repository
 - StoryTag 엔티티의 DB 접근 담당 (TravelStyle) */

@Repository
public interface StoryTagRepository extends JpaRepository<StoryTag, Long> {

        // 특정 게시글의 태그 목록 조회
        List<StoryTag> findByStory_Id(Long storyId);

        // 특정 게시글의 태그 삭제
        void deleteByStory_Id(Long storyId);
}