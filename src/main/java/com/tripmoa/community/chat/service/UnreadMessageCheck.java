package com.tripmoa.community.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnreadMessageCheck {

    private final RedisTemplate<String, String> redisTemplate;

    private String unreadKey(Long roomId, Long userId) {
        return "chat:unread:" + roomId + ":" + userId;
    }

    public void incrementUnread(Long roomId, Long receivedId) {
        redisTemplate.opsForValue().increment(unreadKey(roomId, receivedId));
    }

    public void clearUnread(Long roomId, Long userId) {
        redisTemplate.delete(unreadKey(roomId, userId));
    }

    public int getUnreadCount(Long roomId, Long userId) {
        String val = redisTemplate.opsForValue().get(unreadKey(roomId, userId));
        return val != null ? Integer.parseInt(val) : 0;
    }
}
