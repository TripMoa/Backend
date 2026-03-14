package com.tripmoa.blog.repository;

import com.tripmoa.blog.domain.BlogTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/* 태그 Repository
 - BlogTag 엔티티의 DB 접근 담당 (TravelStyle) */

@Repository
public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {

        // 특정 게시글의 태그 목록 조회
        List<BlogTag> findByBlogId(Long blogId);

        // 특정 게시글의 태그 삭제
        void deleteByBlogId(Long blogId);
}