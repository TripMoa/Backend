package com.tripmoa.story.repository;

import com.tripmoa.story.domain.StoryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/* 댓글 Repository
 - StoryComment 엔티티의 DB 접근 담당 */

@Repository
public interface StoryCommentRepository extends JpaRepository<StoryComment, Long> {

    // 특정 게시글의 댓글 목록 조회
    List<StoryComment> findByStory_Id(Long storyId);

    // 특정 게시글의 댓글 개수 조회
    Long countByStory_Id(Long storyId);

}