package com.tripmoa.mate.service;

import com.tripmoa.mate.repository.MateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MateRepository mateRepository;

    public boolean increaseIfFirstView(Long postId, Long userId) {
        String redisKey = "view:mate:" + postId + ":user:" + userId;
        Boolean isFirst = redisTemplate
                .opsForValue()
                .setIfAbsent(redisKey, "true", Duration.ofHours(24));

        if (Boolean.TRUE.equals(isFirst)) {
            mateRepository.updateViewsCount(postId);
            return true;
        }

        return false;
    }
}
