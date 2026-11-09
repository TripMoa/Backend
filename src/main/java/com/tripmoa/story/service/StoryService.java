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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import com.tripmoa.expense.entity.Expense;
import com.tripmoa.expense.repository.ExpenseRepository;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.story.dto.post.StoryExpenseSummary;

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

    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ObjectMapper objectMapper;

    private final ExpenseRepository expenseRepository;
    private final TripMemberRepository tripMemberRepository;

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

        return StoryResponse.from(story, user, isLiked, commentCount)
                .toBuilder().canEdit(userId != null && canManage(story, userId)).build();
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

            // 여행은 멤버 공동 소유 → 여행당 리뷰 1개 (다른 멤버가 이미 썼어도 막고, 그 리뷰를 같이 수정)
            if (storyRepository.existsByTrip_IdAndType(request.getTripId(), StoryType.REVIEW)) {
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

        //여행 후기 + 연결된 여행이 있으면 동선 스냅샷 저장
        if (type == StoryType.REVIEW && trip != null) {
            story.updateRoute(buildRouteSnapshot(trip.getId()));
        }

        // 여행 후기 + 연결된 여행이 있으면 TRAVEL LOG 경비 요약 저장 (직접 입력값은 쓰지 않음)
        if (type == StoryType.REVIEW && trip != null) {
            applyExpenseSnapshot(story, trip.getId());
        }
        story.updateShowExpense(request.getShowExpense());

        Story saved = storyRepository.save(story);

        return StoryResponse.from(saved, author, false, 0).toBuilder().canEdit(true).build();
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

        // 작성자 본인 또는 (리뷰라면) 그 여행 멤버면 수정 가능
        if (!canManage(story, authorId)) {
            throw new BusinessException(ErrorCode.STORY_UPDATE_FORBIDDEN);
        }

        // 후기 경비는 화면에서 입력받지 않으므로 기존 값을 그대로 둔다 (자유글은 예전처럼 요청값 사용)
        boolean isReview = story.getType() == StoryType.REVIEW;

        story.update(
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                request.getTags(),
                request.getDestination(),
                request.getDuration(),
                request.getDepartureDate(),
                isReview ? story.getTransportation() : request.getTransportation(),
                isReview ? story.getAccommodation() : request.getAccommodation(),
                isReview ? story.getFood() : request.getFood(),
                isReview ? story.getAttraction() : request.getAttraction(),
                isReview ? story.getShopping() : request.getShopping()
        );
        story.updateIsPublic(request.getIsPublic());
        story.updateShowExpense(request.getShowExpense());

        // 후기 수정 시 TRAVEL LOG 경비를 다시 가져온다
        // (직접 입력하던 예전 후기는 TRAVEL LOG 에 지출이 있을 때만 바꿔서, 기존 금액이 0으로 지워지지 않게 함)
        if (isReview && story.getTrip() != null) {
            Long tripId = story.getTrip().getId();
            if (story.getMemberCount() != null || expenseRepository.existsByTrip_Id(tripId)) {
                applyExpenseSnapshot(story, tripId);
            }
        }

        // 기능 추가 전에 작성된 후기는 route가 비어 있음 → 수정 시 한 번 채워줌
        if (story.getRoute() == null && story.getType() == StoryType.REVIEW && story.getTrip() != null) {
            story.updateRoute(buildRouteSnapshot(story.getTrip().getId()));
        }

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isLiked = storyLikeRepository.existsByStory_IdAndUser_Id(id, authorId);
        int commentCount = storyCommentRepository.countByStory_Id(id).intValue();

        return StoryResponse.from(story, user, isLiked, commentCount).toBuilder().canEdit(true).build();
    }

    // 여행기 삭제
    @Transactional
    public void deleteStory(Long id, Long authorId) {

        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 작성자 본인 또는 (리뷰라면) 그 여행 멤버면 삭제 가능
        if (!canManage(story, authorId)) {
            throw new BusinessException(ErrorCode.STORY_DELETE_FORBIDDEN);
        }

        storyRepository.deleteById(id);
    }

    // 특정 사용자가 작성한 여행기 목록 조회
    public List<StoryResponse> getStorysByAuthor(Long authorId) {

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 내가 쓴 글 + 내가 멤버인 여행의 리뷰 (공동 소유라 같이 수정·삭제 가능)
        return storyRepository.findMyStoriesIncludingTripReviews(authorId).stream()
                .map(story -> {
                    boolean isLiked = storyLikeRepository.existsByStory_IdAndUser_Id(story.getId(), authorId);
                    int commentCount = storyCommentRepository.countByStory_Id(story.getId()).intValue();
                    return StoryResponse.from(story, user, isLiked, commentCount).toBuilder().canEdit(true).build();
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

    // 특정 여행의 리뷰 작성 여부 확인 (공동 소유 - 멤버 중 누가 썼든 있으면 true)
    public boolean existsReviewByTripAndAuthor(Long tripId, Long authorId) {
        return storyRepository.existsByTrip_IdAndType(tripId, StoryType.REVIEW);
    }

    // 수정·삭제 권한: 작성자 본인, 또는 여행에 연결된 리뷰라면 그 여행의 멤버
    private boolean canManage(Story story, Long userId) {
        if (userId == null) return false;
        if (story.getAuthor().getId().equals(userId)) return true;
        return story.getType() == StoryType.REVIEW
                && story.getTrip() != null
                && tripMemberRepository.existsByTrip_IdAndUser_Id(story.getTrip().getId(), userId);
    }

    // 후기 작성 페이지 미리보기용 - 저장하지 않고 동선 JSON만 반환
    public String getRoutePreview(Long tripId) {
        return buildRouteSnapshot(tripId);
    }

    // 후기 작성 페이지 미리보기용 - 저장하지 않고 경비 요약만 반환 (여행 멤버만 볼 수 있음, 아니면 null)
    public StoryExpenseSummary getExpensePreview(Long tripId, Long userId) {
        if (userId == null || !tripMemberRepository.existsByTrip_IdAndUser_Id(tripId, userId)) {
            return null;
        }
        return buildExpenseSummary(tripId);
    }

    private void applyExpenseSnapshot(Story story, Long tripId) {
        StoryExpenseSummary s = buildExpenseSummary(tripId);
        story.updateExpenseSnapshot(
                s.getTransportation(), s.getAccommodation(), s.getFood(),
                s.getAttraction(), s.getShopping(), s.getEtc(), s.getMemberCount());
    }

    // TRAVEL LOG 지출을 카테고리별로 합산
    // - 혼자(멤버 1명 이하): 개인 + 공동 지출 전부
    // - 여럿: 공동 지출만 (개인 지출은 남에게 보여줄 금액이 아니라서 제외)
    private StoryExpenseSummary buildExpenseSummary(Long tripId) {
        int memberCount = Math.max(1, tripMemberRepository.countByTrip_Id(tripId));

        List<Expense> expenses = memberCount <= 1
                ? expenseRepository.findAllByTrip_Id(tripId)
                : expenseRepository.findAllByTrip_IdAndSharedTrue(tripId);

        int trans = 0, stay = 0, food = 0, ticket = 0, shop = 0, etc = 0;
        for (Expense e : expenses) {
            int amount = e.getTotalAmount();
            if (e.getCategory() == null) {
                etc += amount;
                continue;
            }
            switch (e.getCategory()) {
                case TRANS -> trans += amount;
                case STAY -> stay += amount;
                case FOOD -> food += amount;
                case TICKET -> ticket += amount;
                case SHOP -> shop += amount;
                default -> etc += amount;
            }
        }

        return StoryExpenseSummary.builder()
                .transportation(trans)
                .accommodation(stay)
                .food(food)
                .attraction(ticket)
                .shopping(shop)
                .etc(etc)
                .total(trans + stay + food + ticket + shop + etc)
                .memberCount(memberCount)
                .build();
    }

    // 여행 일정(Schedule / ScheduleItem)을 JSON 문자열로 복사
    private String buildRouteSnapshot(Long tripId) {
        List<Schedule> schedules = new ArrayList<>(scheduleRepository.findAllByTripId(tripId));
        schedules.sort(Comparator.comparingInt(Schedule::getDay));
 
        List<Map<String, Object>> days = new ArrayList<>();
 
        for (Schedule schedule : schedules) {
            List<ScheduleItem> items = new ArrayList<>(
                    scheduleItemRepository.findAllByScheduleId(schedule.getId()));
            if (items.isEmpty()) continue;
            items.sort(Comparator.comparingInt(ScheduleItem::getOrderIndex));
 
            List<Map<String, Object>> itemList = new ArrayList<>();
            int order = 1;
            for (ScheduleItem item : items) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("order", order++);
                m.put("time", item.getTime());
                m.put("title", item.getTitle());
                m.put("category", item.getCategory());
                m.put("lat", item.getLat());
                m.put("lng", item.getLng());
                m.put("travelMinutes", item.getTravelMinutes());
                itemList.add(m);
            }
 
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("day", schedule.getDay());
            day.put("items", itemList);
            days.add(day);
        }
 
        if (days.isEmpty()) return null;
 
        try {
            return objectMapper.writeValueAsString(days);
        } catch (JsonProcessingException e) {
            // 동선 저장 실패해도 후기 작성은 막지 않음
            return null;
        }
    }
}
