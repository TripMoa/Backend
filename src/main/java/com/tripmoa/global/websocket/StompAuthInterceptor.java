package com.tripmoa.global.websocket;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
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
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

/**
 * STOMP 공통 인터셉터 (채팅·일정 등 모든 WebSocket 기능이 함께 쓴다)
 *
 * - CONNECT  : 헤더의 JWT(액세스 토큰)를 검증하고 사용자를 세션에 심는다.
 *              클라이언트는 연결 헤더에 Authorization: Bearer {token} 을 보내야 한다.
 * - SUBSCRIBE: 주소를 담당하는 StompDestinationPolicy 에 접근 판단을 맡긴다. 담당 정책이 없는 주소는 거부한다.
 * - SEND     : /pub/ 으로 시작하는 주소만 허용한다.
 *
 * 거부 시에는 공용 에러코드 WEBSOCKET_FORBIDDEN 을 쓴다. (채팅 구독 거부는 채팅 정책(ChatDestinationPolicy)이 기존 채팅 코드로 던진다.)
 * 참고: 예외 메시지는 Spring 이 일반 문구("Failed to send message to ...")로 감싸기 때문에, 클라이언트가 받는 STOMP ERROR 프레임에도
 *       기본 서버 로그에도 실리지 않는다. 그래서 이 코드는 "무엇이 거부됐는지"를 코드로 읽을 때를 위한 이름이다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final List<StompDestinationPolicy> destinationPolicies;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscribe(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            validateSend(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.WEBSOCKET_FORBIDDEN);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAccessToken(token)) {
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

    private void validateSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/pub/")) {
            throw new BusinessException(ErrorCode.WEBSOCKET_FORBIDDEN);
        }
    }

    private void validateSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new BusinessException(ErrorCode.WEBSOCKET_FORBIDDEN);
        }

        StompDestinationPolicy policy = destinationPolicies.stream()
                .filter(p -> p.supports(destination))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.WEBSOCKET_FORBIDDEN));

        Principal user = accessor.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.WEBSOCKET_FORBIDDEN);
        }
        Long userId = Long.parseLong(user.getName());

        policy.authorizeSubscribe(userId, destination);
    }
}
