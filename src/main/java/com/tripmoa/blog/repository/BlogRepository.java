package com.tripmoa.blog.repository;

import com.tripmoa.blog.domain.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* 여행기 Repository
 - BlogPost 엔티티의 DB 접근 담당 */

@Repository
public interface BlogRepository extends JpaRepository<BlogPost, Long> {
}