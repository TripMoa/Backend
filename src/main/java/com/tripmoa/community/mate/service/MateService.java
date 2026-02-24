package com.tripmoa.community.mate.service;

import com.tripmoa.community.mate.domain.MateApplication;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.domain.MateDomain;
import com.tripmoa.community.mate.dto.MateRequest;
import com.tripmoa.community.mate.dto.MateResponse;
import com.tripmoa.community.mate.repository.MateRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateService {
    private final MateRepository mateRepository;
    private final UserRepository userRepository;
    private final MateLikeService likeService;
    private final MateDomain domain;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<MateResponse> getMatePosts() {
        List<MatePost> matePosts = mateRepository.findAllWithUser();
        return matePosts.stream()
                .map(post -> {
                    MateResponse response = MateResponse.from(post);
                    Long likeCount = likeService.getLikeCount(post.getId());
                    response.setLikesCount(likeCount);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MateResponse getPostsById(Long id, Long userId) {
        MatePost matePostDetail = mateRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("Post NOT Found"));
        String redisKey = "view:mate:" + id + ":user:" + userId;
        Boolean isFirstView = redisTemplate
                .opsForValue()
                .setIfAbsent(redisKey, "true", Duration.ofHours(24));

        if (Boolean.TRUE.equals(isFirstView)) {
            mateRepository.updateViewsCount(id);
        }

        Long likeCount = likeService.getLikeCount(id);
        boolean isLiked = false;
        String likeUserKey = "post:" + id + ":likeUsers";
        isLiked = Boolean.TRUE.equals(
                redisTemplate
                        .boundSetOps(likeUserKey)
                        .isMember(String.valueOf(userId)));

        MateResponse response = MateResponse.from(matePostDetail);
        response.setLikesCount(likeCount);
        response.setLiked(isLiked);

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
                .orElseThrow(() -> new RuntimeException("Post NOT Found"));

        if(!domain.isAuthor(post, user)) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        mateRepository.deleteById(id);
    }


}
