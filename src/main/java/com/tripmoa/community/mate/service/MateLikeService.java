package com.tripmoa.community.mate.service;

import com.tripmoa.community.mate.dto.LikeResponse;
import com.tripmoa.community.mate.repository.MateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MateLikeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MateRepository mateRepository;

    public LikeResponse toggleLike(Long postId, Long userId) {

        String likeUserKey = "post:" + postId + ":likeUsers";
        String likeCountKey = "post:" + postId + ":likeCount";
        String userIdStr = String.valueOf(userId);

        BoundSetOperations<String, Object> setOps =  redisTemplate.boundSetOps(likeUserKey);
        boolean alreadyLiked = Boolean.TRUE.equals(setOps.isMember(userIdStr));

        if(alreadyLiked) {
            setOps.remove(userIdStr);
            Long count = redisTemplate.opsForValue().decrement(likeCountKey);
            return new LikeResponse(false, count);
        } else {
            setOps.add(userIdStr);
            Long count = redisTemplate.opsForValue().increment(likeCountKey);
            return new LikeResponse(true, count);
        }
    }

    public Long getLikeCount(Long postId) {
        String likeCountKey = "post:" + postId + ":likeCount";
        Object value = redisTemplate.opsForValue().get(likeCountKey);

        if (value == null) {
            Long fromDB = mateRepository.findById(postId)
                    .map(post -> post.getLikesCount())
                    .orElse(0L);

            redisTemplate.opsForValue().set(likeCountKey, fromDB);
            return fromDB;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
