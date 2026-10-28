package com.tripmoa.chat.websocket;

import com.tripmoa.chat.repository.ChatRoomRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.global.websocket.StompDestinationPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 채팅방 구독 규칙: /sub/chat/room/{roomId} 는 그 채팅방에 아직 남아 있는 참여자만 구독할 수 있다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatDestinationPolicy implements StompDestinationPolicy {

    private static final String ROOM_PREFIX = "/sub/chat/room/";

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public boolean supports(String destination) {
        return destination != null && destination.startsWith(ROOM_PREFIX);
    }

    @Override
    public void authorizeSubscribe(Long userId, String destination) {
        Long roomId;
        try {
            roomId = Long.parseLong(destination.substring(ROOM_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS);
        }

        if (!chatRoomRepository.existsActiveMember(roomId, userId)) {
            log.warn("SUBSCRIBE 거부 userId={}, roomId={}", userId, roomId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS);
        }
    }
}
