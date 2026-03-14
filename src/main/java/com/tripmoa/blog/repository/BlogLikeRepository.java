package com.tripmoa.blog.repository;

import com.tripmoa.blog.domain.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* 좋아요 Repository
 - BlogLike 엔티티의 DB 접근 담당 */

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, Long> {

    // 특정 게시글에 사용자가 좋아요를 눌렀는지 확인
    boolean existsByBlogIdAndUserId(Long blogId, Long userId);

    // 특정 게시글의 좋아요 삭제
    void deleteByBlogIdAndUserId(Long blogId, Long userId);

    // 특정 게시글의 좋아요 개수 조회
    Long countByBlogId(Long blogId);
}