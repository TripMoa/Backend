package com.tripmoa.mate.service;

import com.tripmoa.global.util.BadWordFilter;
import com.tripmoa.mate.domain.MatePost;
import com.tripmoa.mate.domain.MateDomain;
import com.tripmoa.mate.dto.MateRequest;
import com.tripmoa.mate.dto.MateResponse;
import com.tripmoa.mate.repository.MateRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.matetag.event.MatePostCreatedEvent;
import com.tripmoa.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateService {
    private final MateRepository mateRepository;
    private final MateLikeService likeService;
    private final MateApplicationService applyService;
    private final PassedPostService passedPostService;
    private final ViewCountService viewCountService;
    private final MateDomain domain;
    private final BadWordFilter badWordFilter;
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<MateResponse> getMatePosts(Long userId) {
        List<MatePost> matePosts = mateRepository.findAllWithUser();
        return matePosts.stream()
                .map(post -> {
                    MateResponse response = MateResponse.from(post);
                    response.setLikesCount(likeService.getLikeCount(post.getId()));
                    response.setLiked(userId != null && likeService.isLikedByUser(post.getId(), userId));

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MateResponse getPostsById(Long id, Long userId) {
        MatePost matePostDetail = mateRepository.findByIdWithUser(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        viewCountService.increaseIfFirstView(id, userId);

        MateResponse response = MateResponse.from(matePostDetail);
        response.setLikesCount(likeService.getLikeCount(id));
        response.setLiked(userId != null && likeService.isLikedByUser(id, userId));
        response.setHasApplied(userId != null && applyService.hasApplied(id, userId));

        return response;
    }

    @Transactional
    public MateResponse createPost(MateRequest request, User user) {
        if (badWordFilter.containsBadWord(request.getContent())
                || badWordFilter.containsBadWord(request.getDestination())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        domain.validateProfileCompleteness(user);
        MatePost post = request.toEntity(user);

        domain.validateCreate(post);
        mateRepository.save(post);
        applicationEventPublisher.publishEvent(
                new MatePostCreatedEvent(
                        post.getId(),
                        post.getContent(),
                        post.getDestination()));
        return MateResponse.from(post);
    }

    public void deletePostById(Long id, User user) {
        MatePost post = this.mateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if(!domain.isAuthor(post, user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }
        mateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MateResponse> getExpiredPosts(Pageable pageable) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return mateRepository
                .findByEndDateBeforeOrderByEndDateDesc(today, pageable)
                .map(MateResponse::from)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<MateResponse> getPassedPosts(Long userId) {
        Set<Long> ids = passedPostService.getPassedIds(userId);
        if (ids.isEmpty()) return List.of();
        return mateRepository.findAllById(ids).stream()
                .map(MateResponse::from)
                .toList();
    }


}
