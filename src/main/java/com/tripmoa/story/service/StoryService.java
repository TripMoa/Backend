package com.tripmoa.story.service;

import com.tripmoa.story.domain.Story;
import com.tripmoa.story.dto.post.StoryRequest;
import com.tripmoa.story.dto.post.StoryResponse;
import com.tripmoa.story.dto.post.StoryUpdateRequest;
import com.tripmoa.story.enums.StoryType;
import com.tripmoa.story.repository.StoryLikeRepository;
import com.tripmoa.story.repository.StoryRepository;
import com.tripmoa.story.repository.StoryCommentRepository;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.repository.TripRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tripmoa.global.util.FastApiBadWordClient;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;

/* 여행기 서비스
 - 여행기 조회, 생성, 수정, 삭제 기능 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final StoryCommentRepository storyCommentRepository;
    private final FastApiBadWordClient fastApiBadWordClient;
    private final TripRepository tripRepository;

    // 전체 여행기 목록 조회
    public List<StoryResponse> getStorys(Long userId, String tag) {

        List<Story> stories;

        if (tag == null) {
            stories = storyRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .filter(s -> s.getType() == StoryType.FREE || Boolean.TRUE.equals(s.getIsPublic()))
                    .collect(Collectors.toList());
        } else {
            stories = storyRepository.findByTagsContaining("," + tag + ",")
                    .stream()
                    .filter(s -> s.getType() == StoryType.FREE || Boolean.TRUE.equals(s.getIsPublic()))
                    .collect(Collectors.toList());
        }

        return stories.stream()
                .map(story -> {
                    User user = story.getAuthor();

                    boolean isLiked = userId != null &&
                            storyLikeRepository.existsByStory_IdAndUser_Id(story.getId(), userId);

                    int commentCount = storyCommentRepository.countByStory_Id(story.getId()).intValue();

                    return StoryResponse.from(story, user, isLiked, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 여행기 상세 조회
    @Transactional
    public StoryResponse getStory(Long id, Long userId) {

        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 조회수 증가
        story.incrementViews();

        User user = story.getAuthor();

        boolean isLiked = userId != null &&
                storyLikeRepository.existsByStory_IdAndUser_Id(id, userId);

        int commentCount = storyCommentRepository.countByStory_Id(id).intValue();

        return StoryResponse.from(story, user, isLiked, commentCount);
    }

    // 여행기 생성
    @Transactional
    public StoryResponse createStory(StoryRequest request, Long authorId) {

        // 욕설 필터 검사
        if (fastApiBadWordClient.checkBadWord(request.getTitle()) ||
                fastApiBadWordClient.checkBadWord(request.getDescription())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Trip 연결
        Trip trip = null;
        if (request.getTripId() != null) {
            trip = tripRepository.findById(request.getTripId()).orElse(null);

            // 이미 해당 trip으로 작성된 리뷰 있는지 확인
            if (storyRepository.existsByTrip_IdAndAuthor_Id(request.getTripId(), authorId)) {
                throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
            }
        }

        String tags = request.getTagIds() != null
                ? request.getTagIds().toString()
                : request.getTags();

        StoryType type = request.getType() != null
                ? StoryType.valueOf(request.getType())
                : StoryType.FREE;

        Story story = new Story(
                author,
                trip,
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                tags,
                request.getDestination(),
                request.getDuration(),
                request.getDepartureDate(),
                request.getTransportation(),
                request.getAccommodation(),
                request.getFood(),
                request.getAttraction(),
                request.getShopping(),
                type,
                request.getIsPublic()

        );

        Story saved = storyRepository.save(story);

        return StoryResponse.from(saved, author, false, 0);
    }

    // 여행기 수정
    @Transactional
    public StoryResponse updateStory(Long id, StoryUpdateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (fastApiBadWordClient.checkBadWord(request.getTitle()) ||
                fastApiBadWordClient.checkBadWord(request.getDescription())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!story.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.STORY_UPDATE_FORBIDDEN);
        }

        story.update(
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
        story.updateIsPublic(request.getIsPublic());

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isLiked = storyLikeRepository.existsByStory_IdAndUser_Id(id, authorId);
        int commentCount = storyCommentRepository.countByStory_Id(id).intValue();

        return StoryResponse.from(story, user, isLiked, commentCount);
    }

    // 여행기 삭제
    @Transactional
    public void deleteStory(Long id, Long authorId) {

        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 작성자 본인만 삭제 가능
        if (!story.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.STORY_DELETE_FORBIDDEN);
        }

        storyRepository.deleteById(id);
    }

    // 특정 사용자가 작성한 여행기 목록 조회
    public List<StoryResponse> getStorysByAuthor(Long authorId) {

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return storyRepository.findByAuthor_IdOrderByCreatedAtDesc(authorId).stream()
                .map(story -> {
                    boolean isLiked = storyLikeRepository.existsByStory_IdAndUser_Id(story.getId(), authorId);
                    int commentCount = storyCommentRepository.countByStory_Id(story.getId()).intValue();
                    return StoryResponse.from(story, user, isLiked, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 조회수 증가
    @Transactional
    public void incrementViews(Long id) {

        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        story.incrementViews();
    }

    // 좋아요한 여행기 목록 조회
    public List<StoryResponse> getLikedStories(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return storyLikeRepository.findByUser_Id(userId).stream()
                .map(like -> {
                    Story story = like.getStory();
                    int commentCount = storyCommentRepository.countByStory_Id(story.getId()).intValue();
                    return StoryResponse.from(story, story.getAuthor(), true, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 특정 여행의 리뷰 작성 여부 확인
    public boolean existsReviewByTripAndAuthor(Long tripId, Long authorId) {
        return storyRepository.existsByTrip_IdAndAuthor_Id(tripId, authorId);
    }
}