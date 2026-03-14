package com.tripmoa.blog.repository;

import com.tripmoa.blog.domain.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/* 댓글 Repository
 - BlogComment 엔티티의 DB 접근 담당 */

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    // 특정 게시글의 댓글 목록 조회
    List<BlogComment> findByBlogId(Long blogId);

    // 특정 게시글의 댓글 개수 조회
    Long countByBlogId(Long blogId);

}