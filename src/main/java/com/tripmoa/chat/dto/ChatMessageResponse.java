package com.tripmoa.chat.dto;

import com.tripmoa.chat.domain.ChatMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponse {

    private String id;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String timestamp;
    private String type;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(String.valueOf(message.getId()))
                .senderId(message.getSender().getEmail())
                .senderName(message.getSender().getName())
                .senderAvatar(message.getSender().getProfileImage())
                .content(message.getContent())
                .timestamp(message.getCreatedAt().toString())
                .type(message.getType().name())
                .build();
    }
}