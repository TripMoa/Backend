package com.tripmoa.mate.service;

import com.tripmoa.mate.domain.MatePost;
import com.tripmoa.mate.domain.MateDomain;
import com.tripmoa.mate.dto.MateRequest;
import com.tripmoa.mate.dto.MateResponse;
import com.tripmoa.mate.repository.ApplicationRepository;
import com.tripmoa.mate.repository.MateRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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

    public List<MateResponse> getMatePosts(Long userId) {
        List<MatePost> matePosts = mateRepository.findAllWithUser();
        return matePosts.stream()
                .map(post -> {
                    MateResponse response = MateResponse.from(post);
                    response.setLikesCount(likeService.getLikeCount(post.getId()));
                    response.setLiked(likeService.isLikedByUser(post.getId(), userId));

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
        response.setLiked(likeService.isLikedByUser(id, userId));
        response.setHasApplied(applyService.hasApplied(id, userId));

        return response;
    }

    @Transactional
    public MateResponse createPost(MateRequest request, User user) {
        MatePost post = request.toEntity(user);

        domain.validateCreate(post);
        mateRepository.save(post);
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
