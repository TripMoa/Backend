package com.tripmoa.chat.controller;

import com.tripmoa.chat.dto.ChatMessageResponse;
import com.tripmoa.chat.dto.ChatRequest;
import com.tripmoa.chat.service.ChatService;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP 메시지 핸들러
 *
 * 클라이언트 → 서버: /pub/chat/message  (메시지 전송)
 * 서버 → 클라이언트: /sub/chat/room/{roomId}  (메시지 수신 구독)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StompController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    // 클라이언트가 /pub/chat/message 로 메시지를 보내면
    // DB에 저장 후 해당 채팅방 구독자들에게 브로드캐스트
    @MessageMapping("/chat/message")
    public void sendMessage(ChatRequest.SendMessage request, Principal principal) {
        // 인증된 사용자 조회
        User sender = userRepository.findById(Long.parseLong(principal.getName()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // DB 저장
        ChatMessageResponse response = chatService.sendMessage(
                request.getChatRoomId(), request.getContent(), sender);

        // 해당 채팅방 구독자에게 브로드캐스트
        String destination = "/sub/chat/room/" + request.getChatRoomId();
        messagingTemplate.convertAndSend(destination, response);
    }
}