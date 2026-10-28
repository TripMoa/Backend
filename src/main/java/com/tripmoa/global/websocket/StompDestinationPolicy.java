package com.tripmoa.global.websocket;

/**
 * STOMP 구독 주소별 접근 규칙.
 *
 * 공통 인터셉터(StompAuthInterceptor)가 로그인(JWT)만 확인하고, "이 주소를 이 사용자가 구독해도 되는가"는
 * 주소를 담당하는 도메인의 정책이 판단한다. 새 구독 주소가 필요하면 이 인터페이스를 구현한 @Component를 하나 추가하면 되고,
 * 공통 인터셉터는 고치지 않는다. 어떤 정책도 담당하지 않는 주소는 거부된다.
 *
 * 구현 시 주의: 주소에 들어 있는 id는 숫자로 엄격하게 해석하고, 해석에 실패하면(와일드카드 패턴 등) 거부한다.
 */
public interface StompDestinationPolicy {

    // 이 정책이 담당하는 구독 주소인가 (예: "/sub/chat/room/" 로 시작)
    boolean supports(String destination);

    // 구독 허용 여부 판단. 허용하지 않으면 BusinessException 을 던진다.
    void authorizeSubscribe(Long userId, String destination);
}
