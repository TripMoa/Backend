package com.tripmoa.blog.service;

import com.tripmoa.blog.domain.SavedItinerary;
import com.tripmoa.blog.domain.BlogPost;
import com.tripmoa.blog.dto.post.BlogResponse;
import com.tripmoa.blog.repository.SavedItineraryRepository;
import com.tripmoa.blog.repository.BlogRepository;
import com.tripmoa.blog.repository.BlogLikeRepository;
import com.tripmoa.blog.repository.BlogCommentRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/* 저장된 일정 서비스
 - 여행기 일정 저장, 해제, 조회 기능 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedItineraryService {

    private final SavedItineraryRepository savedItineraryRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogCommentRepository blogCommentRepository;

    // 일정 저장 (USE THIS ITINERARY)
    @Transactional
    public void saveItinerary(Long blogId, Long userId) {

        // 이미 저장된 일정인지 확인
        if (savedItineraryRepository.existsByBlogIdAndUserId(blogId, userId)) {
            return;
        }

        // 여행기 존재 확인
        blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("여행기를 찾을 수 없습니다."));

        // 일정 저장
        SavedItinerary savedItinerary = new SavedItinerary(blogId, userId);
        savedItineraryRepository.save(savedItinerary);
    }

    // 일정 저장 해제
    @Transactional
    public void unsaveItinerary(Long blogId, Long userId) {
        savedItineraryRepository.deleteByBlogIdAndUserId(blogId, userId);
    }

    // 일정 저장 여부 확인
    public boolean isSaved(Long blogId, Long userId) {
        return savedItineraryRepository.existsByBlogIdAndUserId(blogId, userId);
    }

    // 저장된 일정 목록 조회
    public List<BlogResponse> getSavedItineraries(Long userId) {

        List<SavedItinerary> savedList = savedItineraryRepository.findByUserId(userId);

        return savedList.stream()
                .map(saved -> {

                    // 저장된 여행기 조회
                    BlogPost blog = blogRepository.findById(saved.getBlogId())
                            .orElse(null);
                    if (blog == null) return null;

                    // 작성자 정보 조회
                    User author = userRepository.findById(blog.getAuthorId())
                            .orElse(null);
                    if (author == null) return null;

                    boolean isLiked = blogLikeRepository.existsByBlogIdAndUserId(blog.getId(), userId);
                    int commentCount = blogCommentRepository.countByBlogId(blog.getId()).intValue();

                    return BlogResponse.from(blog, author, isLiked, commentCount);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
}