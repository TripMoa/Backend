package com.tripmoa.community.chat.service;

import com.tripmoa.community.chat.domain.ChatMessage;
import com.tripmoa.community.chat.domain.ChatRoom;
import com.tripmoa.community.chat.domain.MessageType;
import com.tripmoa.community.chat.dto.ChatMessageResponse;
import com.tripmoa.community.chat.dto.ChatRoomResponse;
import com.tripmoa.community.chat.repository.ChatMessageRepository;
import com.tripmoa.community.chat.repository.ChatRoomRepository;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.repository.MateRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MateRepository mateRepository;
    private final UserRepository userRepository;
    private final UnreadMessageCheck unreadMessageCheck;
    private final SimpMessagingTemplate messagingTemplate;

     // 채팅방 생성 (메이트 신청 시 호출)
     // 이미 존재하면 기존 채팅방 반환
     // 자기 자신의 게시글에는 채팅방 생성 불가
    @Transactional
    public ChatRoomResponse createChatRoom(Long matePostId, User requestUser, Long applicantId) {
        MatePost post = mateRepository.findById(matePostId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + matePostId));

        User author = post.getUser();
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("신청자를 찾을 수 없습니다"));

        // 자기 자신에게 채팅 신청 방지
        if (author.getId().equals(applicant.getId())) {
            throw new IllegalArgumentException("자기 자신에게 채팅을 신청 할 수 없습니다");
        }

        // 이미 존재하는 채팅방이면 그대로 반환
        return chatRoomRepository.findByMatePostIdAndApplicantId(matePostId, applicant.getId())
                .map(room -> ChatRoomResponse.from(room, 0))
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .matePost(post)
                            .author(author)
                            .applicant(applicant)
                            .lastMessageAt(LocalDateTime.now())
                            .build();

                    ChatRoom saved = chatRoomRepository.save(room);

                    return ChatRoomResponse.from(saved, 0);
                });
    }

    // 내 채팅방 목록 조회
    public List<ChatRoomResponse> getMyChatRooms(User member) {
        return chatRoomRepository.findAllByMemberNotLeft(member).stream()
                .map(room -> ChatRoomResponse.from(
                        room,
                        unreadMessageCheck.getUnreadCount(room.getId(), member.getId())
                ))
                .toList();
    }

    // 특정 채팅방 조회 (권한 검증 포함)
    public ChatRoomResponse getChatRoom(Long roomId, User member) {
        ChatRoom room = findRoomOrThrow(roomId);
        validateMembership(room, member);
        unreadMessageCheck.clearUnread(roomId, member.getId());
        return ChatRoomResponse.from(room, 0);
    }

    // 메시지 전송 (REST API용 - STOMP 사용 시에도 DB 저장용으로 호출)
    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, String content, User sender) {
        ChatRoom room = findRoomOrThrow(chatRoomId);
        validateMembership(room, sender);

        if (room.hasLeft(sender)) {
            throw new IllegalStateException("이미 나간 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .type(MessageType.CHAT)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        room.updateLastMessageAt(saved.getCreatedAt());

        User receiver = room.getOtherMember(sender);
        unreadMessageCheck.incrementUnread(chatRoomId, receiver.getId());

        return ChatMessageResponse.from(saved);
    }

    // 특정 채팅방의 메시지 목록 조회
    public List<ChatMessageResponse> getMessages(Long chatRoomId, User member) {
        ChatRoom room = findRoomOrThrow(chatRoomId);
        validateMembership(room, member);

        return chatMessageRepository.findAllByChatRoomId(chatRoomId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public void leaveRoom(Long roomId, User member) {
         ChatRoom room = findRoomOrThrow(roomId);
         validateMembership(room, member);

         room.markLeft(member);

        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(member)
                .content(member.getName() + "님이 채팅방을 나갔습니다.")
                .type(MessageType.LEAVE)
                .build();
        chatMessageRepository.save(systemMessage);
        room.updateLastMessageAt(systemMessage.getCreatedAt());

        if (room.bothLeft()) {
            chatRoomRepository.delete(room);
        }

        unreadMessageCheck.clearUnread(roomId, member.getId());

        // 상대방에게 나가기 알림
        ChatMessageResponse response = ChatMessageResponse.from(systemMessage);
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, response);
    }

    // === Private 헬퍼 메서드 ===

    private ChatRoom findRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
    }

    private void validateMembership(ChatRoom room, User member) {
        if (!room.isMember(member)) {
            throw new SecurityException("이 채팅방에 접근할 권한이 없습니다");
        }
    }
}