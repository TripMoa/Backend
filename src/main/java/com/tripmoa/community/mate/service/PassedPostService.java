package com.tripmoa.community.mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassedPostService {
    private final StringRedisTemplate redis;
    private static final long TTL_MS = 24 * 60 * 60 * 1000L;

    private String key(Long userId) { return "mate:passed:" + userId; }

    public void pass(Long userId, Long postId) {
        long now = System.currentTimeMillis();
        String k = key(userId);
        redis.opsForZSet().add(k, postId.toString(), now);
        redis.opsForZSet().removeRangeByScore(k, 0, now - TTL_MS);
    }

    public void unpass(Long userId, Long postId) {
        redis.opsForZSet().remove(key(userId), postId.toString());
    }

    public Set<Long> getPassedIds(Long userId) {
        long now = System.currentTimeMillis();
        String k = key(userId);
        redis.opsForZSet().removeRangeByScore(k, 0, now - TTL_MS);
        Set<String> ids = redis.opsForZSet().rangeByScore(k, now - TTL_MS, now);
        return ids == null ? Set.of()
                : ids.stream().map(Long::parseLong).collect(Collectors.toSet());
    }

}
