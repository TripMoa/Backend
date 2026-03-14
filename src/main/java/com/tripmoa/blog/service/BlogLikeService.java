package com.tripmoa.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripmoa.blog.domain.BlogLike;
import com.tripmoa.blog.domain.BlogPost;
import com.tripmoa.blog.repository.BlogLikeRepository;
import com.tripmoa.blog.repository.BlogRepository;

/* 좋아요 서비스
 - 게시글 좋아요 추가 및 취소 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogLikeService {

    private final BlogLikeRepository blogLikeRepository;
    private final BlogRepository blogRepository;

    // 좋아요 토글 (추가 또는 취소)
    @Transactional
    public boolean toggleLike(Long blogId, Long userId) {

        BlogPost post = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("글을 찾을 수 없습니다."));

        // 이미 좋아요를 눌렀으면 취소
        if (blogLikeRepository.existsByBlogIdAndUserId(blogId, userId)) {

            blogLikeRepository.deleteByBlogIdAndUserId(blogId, userId);
            post.decrementLikes();
            return false;

        } else {

            // 좋아요 추가
            BlogLike like = new BlogLike(blogId, userId);
            blogLikeRepository.save(like);
            post.incrementLikes();
            return true;
        }
    }
}