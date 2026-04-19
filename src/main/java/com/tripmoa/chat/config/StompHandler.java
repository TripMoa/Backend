package com.tripmoa.chat.config;

import com.tripmoa.security.jwt.JwtTokenProvider;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.security.principal.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

 // STOMP 연결 시 JWT 토큰 검증 인터셉터
 // 클라이언트에서 WebSocket 연결 시 헤더에 JWT 토큰을 포함해야 함:
 // stompClient.connect({ Authorization: "Bearer {token}" }, ...)
@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

     private final JwtTokenProvider jwtTokenProvider;
     private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Authorization 헤더가 없습니다");
            }

            String token = authHeader.substring(7);

            try {
                // 프로젝트 기존 메서드 그대로 사용
                if (!jwtTokenProvider.validateToken(token)) {
                    throw new IllegalArgumentException("유효하지 않은 토큰입니다");
                }

                Long userId = jwtTokenProvider.getUserId(token);
                CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                accessor.setUser(auth);

            } catch (Exception e) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다");
            }
        }

        return message;
    }
}