package com.tripmoa.story.repository;

import com.tripmoa.story.domain.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* 좋아요 Repository
 - StoryLike 엔티티의 DB 접근 담당 */

@Repository
public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {

    // 특정 게시글에 사용자가 좋아요를 눌렀는지 확인
    boolean existsByStory_IdAndUser_Id(Long storyId, Long userId);

    // 특정 게시글의 좋아요 삭제
    void deleteByStory_IdAndUser_Id(Long storyId, Long userId);

    // 특정 게시글의 좋아요 개수 조회
    Long countByStory_Id(Long storyId);
}