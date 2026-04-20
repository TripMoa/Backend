package com.tripmoa.story.service;

import com.tripmoa.story.domain.SavedItinerary;
import com.tripmoa.story.domain.Story;
import com.tripmoa.story.dto.post.StoryResponse;
import com.tripmoa.story.repository.SavedItineraryRepository;
import com.tripmoa.story.repository.StoryRepository;
import com.tripmoa.story.repository.StoryLikeRepository;
import com.tripmoa.story.repository.StoryCommentRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;

/* 저장된 일정 서비스
 - 여행기 일정 저장, 해제, 조회 기능 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedItineraryService {

    private final SavedItineraryRepository savedItineraryRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final StoryCommentRepository storyCommentRepository;

    // 일정 저장 (USE THIS ITINERARY)
    @Transactional
    public void saveItinerary(Long storyId, Long userId) {

        // 이미 저장된 일정인지 확인
        if (savedItineraryRepository.existsByStory_IdAndUser_Id(storyId, userId)) {
            throw new BusinessException(ErrorCode.SAVED_ITINERARY_ALREADY_EXISTS);
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        SavedItinerary savedItinerary = new SavedItinerary(story, user);

        savedItineraryRepository.save(savedItinerary);
    }

    // 일정 저장 해제
    @Transactional
    public void unsaveItinerary(Long storyId, Long userId) {
        savedItineraryRepository.deleteByStory_IdAndUser_Id(storyId, userId);
    }

    // 일정 저장 여부 확인
    public boolean isSaved(Long storyId, Long userId) {
        return savedItineraryRepository.existsByStory_IdAndUser_Id(storyId, userId);
    }

    // 저장된 일정 목록 조회
    public List<StoryResponse> getSavedItineraries(Long userId) {

        List<SavedItinerary> savedList = savedItineraryRepository.findByUser_Id(userId);

        return savedList.stream()
                .map(saved -> {

                    Story story = saved.getStory();
                    User author = story.getAuthor();

                    boolean isLiked = storyLikeRepository.existsByStory_IdAndUser_Id(story.getId(), userId);
                    int commentCount = storyCommentRepository.countByStory_Id(story.getId()).intValue();

                    return StoryResponse.from(story, author, isLiked, commentCount);
                })
                .collect(Collectors.toList());
    }
}