package com.tripmoa.community.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRequest {

    @Getter
    @NoArgsConstructor
    public static class CreateRoom {

        @NotNull(message = "게시글 ID는 필수입니다")
        private Long matePostId;

        private Long applicantId;
    }

     // STOMP 메시지 전송 요청
     // 프론트에서 /pub/chat/message 로 보낼 때 사용
    @Getter
    @NoArgsConstructor
    public static class SendMessage {

        @NotNull(message = "채팅방 ID는 필수입니다")
        private Long chatRoomId;

        @NotBlank(message = "메시지 내용은 필수입니다")
        private String content;
    }
}