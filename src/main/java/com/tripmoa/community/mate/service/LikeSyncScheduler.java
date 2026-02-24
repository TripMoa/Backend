package com.tripmoa.community.mate.service;

import com.tripmoa.community.mate.repository.MateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MateRepository mateRepository;

    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void syncLikesToDB() {

        ScanOptions options = ScanOptions.scanOptions()
                .match("post:*:likeCount")
                .count(100)
                .build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options)) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Long postId = extractPostId(key);

                String countStr = (String) redisTemplate.opsForValue().get(key);
                if (countStr == null) continue;

                Long likeCount = Long.valueOf(countStr);

                mateRepository.updateLikeCount(postId, likeCount);

                log.info("Synced Redis to DB | postId={}, count={}", postId, likeCount);
            }
        }

    }

    private Long extractPostId(String key) {
        return Long.valueOf(key.split(":")[1]);
    }

}
