package com.tripmoa.story.service;

import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripmoa.story.domain.StoryLike;
import com.tripmoa.story.domain.Story;
import com.tripmoa.story.repository.StoryLikeRepository;
import com.tripmoa.story.repository.StoryRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.entity.User;

/* 좋아요 서비스
 - 게시글 좋아요 추가 및 취소 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryLikeService {

    private final StoryLikeRepository storyLikeRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    // 좋아요 토글 (추가 또는 취소)
    @Transactional
    public boolean toggleLike(Long storyId, Long userId) {

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 좋아요를 눌렀으면 취소
        if (storyLikeRepository.existsByStory_IdAndUser_Id(storyId, userId)) {

            storyLikeRepository.deleteByStory_IdAndUser_Id(storyId, userId);
            story.decrementLikes();
            return false;

        } else {

            // 좋아요 추가
            StoryLike like = new StoryLike(story, user);
            storyLikeRepository.save(like);
            story.incrementLikes();
            return true;
        }
    }
}