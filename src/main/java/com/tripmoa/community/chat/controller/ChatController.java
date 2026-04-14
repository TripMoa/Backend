package com.tripmoa.community.chat.controller;

import com.tripmoa.community.chat.dto.ChatMessageResponse;
import com.tripmoa.community.chat.dto.ChatRequest;
import com.tripmoa.community.chat.dto.ChatRoomResponse;
import com.tripmoa.community.chat.service.ChatService;
import com.tripmoa.community.chat.service.UnreadMessageCheck;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UnreadMessageCheck unreadMessageCheck;

    // 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(
            @Valid @RequestBody ChatRequest.CreateRoom request,
            @AuthenticationPrincipal CustomUserDetails userDetails   // 로그인한 유저와 글 쓴 유저 일치하는지 확인
    ) {
        User user = userDetails.getUser();
        ChatRoomResponse response = chatService.createChatRoom(request.getMatePostId(), user, request.getApplicantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User member = userDetails.getUser();
        return ResponseEntity.ok(chatService.getMyChatRooms(member));
    }

    // 채팅방 상세 조회
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User member = userDetails.getUser();
        return ResponseEntity.ok(chatService.getChatRoom(roomId, member));
    }

    // 채팅방 메세지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User member = userDetails.getUser();
        return ResponseEntity.ok(chatService.getMessages(roomId, member));
    }

    // 메세지 전송
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody ChatRequest.SendMessage request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User member = userDetails.getUser();
        ChatMessageResponse response = chatService.sendMessage(roomId, request.getContent(), member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        unreadMessageCheck.clearUnread(roomId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        chatService.leaveRoom(roomId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}