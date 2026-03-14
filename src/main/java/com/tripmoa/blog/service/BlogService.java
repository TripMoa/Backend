package com.tripmoa.blog.service;

import com.tripmoa.blog.domain.BlogPost;
import com.tripmoa.blog.dto.post.BlogCreateRequest;
import com.tripmoa.blog.dto.post.BlogResponse;
import com.tripmoa.blog.dto.post.BlogUpdateRequest;
import com.tripmoa.blog.repository.BlogLikeRepository;
import com.tripmoa.blog.repository.BlogRepository;
import com.tripmoa.blog.repository.BlogCommentRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tripmoa.global.util.BadWordFilter;

import java.util.List;
import java.util.stream.Collectors;

/* 여행기 서비스
 - 여행기 조회, 생성, 수정, 삭제 기능 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final BadWordFilter badWordFilter;

    // 전체 여행기 목록 조회
    public List<BlogResponse> getBlogs(Long userId) {
        return blogRepository.findAll().stream()
                .map(blog -> {
                    User user = userRepository.findById(blog.getAuthorId())
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                    boolean isLiked = userId != null &&
                            blogLikeRepository.existsByBlogIdAndUserId(blog.getId(), userId);

                    int commentCount = blogCommentRepository.countByBlogId(blog.getId()).intValue();

                    return BlogResponse.from(blog, user, isLiked, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 여행기 상세 조회
    @Transactional
    public BlogResponse getBlog(Long id, Long userId) {

        BlogPost blogPost = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("글을 찾을 수 없습니다. id: " + id));

        // 조회수 증가
        blogPost.incrementViews();

        User user = userRepository.findById(blogPost.getAuthorId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        boolean isLiked = userId != null &&
                blogLikeRepository.existsByBlogIdAndUserId(id, userId);

        int commentCount = blogCommentRepository.countByBlogId(id).intValue();

        return BlogResponse.from(blogPost, user, isLiked, commentCount);
    }

    // 여행기 생성
    @Transactional
    public BlogResponse createBlog(BlogCreateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (badWordFilter.containsBadWord(request.getTitle()) ||
                badWordFilter.containsBadWord(request.getDescription())) {
            throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다.");
        }

        BlogPost blogPost = new BlogPost(
                authorId,
                request.getTripId(),
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                request.getTags(),
                request.getDestination(),
                request.getDuration(),
                request.getDepartureDate(),
                request.getTransportation(),
                request.getAccommodation(),
                request.getFood(),
                request.getAttraction(),
                request.getShopping()
        );

        BlogPost saved = blogRepository.save(blogPost);

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return BlogResponse.from(saved, user, false, 0);
    }

    // 여행기 수정
    @Transactional
    public BlogResponse updateBlog(Long id, BlogUpdateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (badWordFilter.containsBadWord(request.getTitle()) ||
                badWordFilter.containsBadWord(request.getDescription())) {
            throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다.");
        }

        BlogPost blogPost = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("글을 찾을 수 없습니다. id: " + id));

        // 작성자 본인만 수정 가능
        if (!blogPost.getAuthorId().equals(authorId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        blogPost.update(
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                request.getTags(),
                request.getDestination(),
                request.getDuration(),
                request.getDepartureDate(),
                request.getTransportation(),
                request.getAccommodation(),
                request.getFood(),
                request.getAttraction(),
                request.getShopping()
        );

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        boolean isLiked = blogLikeRepository.existsByBlogIdAndUserId(id, authorId);
        int commentCount = blogCommentRepository.countByBlogId(id).intValue();

        return BlogResponse.from(blogPost, user, isLiked, commentCount);
    }

    // 여행기 삭제
    @Transactional
    public void deleteBlog(Long id, Long authorId) {

        BlogPost blogPost = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("글을 찾을 수 없습니다. id: " + id));

        // 작성자 본인만 삭제 가능
        if (!blogPost.getAuthorId().equals(authorId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        blogRepository.deleteById(id);
    }

    // 특정 사용자가 작성한 여행기 목록 조회
    public List<BlogResponse> getBlogsByAuthor(Long authorId) {

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return blogRepository.findAll().stream()
                .filter(blog -> blog.getAuthorId().equals(authorId))
                .map(blog -> {

                    boolean isLiked = blogLikeRepository.existsByBlogIdAndUserId(blog.getId(), authorId);
                    int commentCount = blogCommentRepository.countByBlogId(blog.getId()).intValue();

                    return BlogResponse.from(blog, user, isLiked, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 조회수 증가
    @Transactional
    public void incrementViews(Long id) {

        BlogPost blogPost = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("글을 찾을 수 없습니다."));

        blogPost.incrementViews();
    }

    // 좋아요한 여행기 목록 조회
    public List<BlogResponse> getLikedStories(Long userId) {
        return List.of();
    }
}