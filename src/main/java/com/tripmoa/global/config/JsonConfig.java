package com.tripmoa.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JsonConfig
 *
 * - Jackson ObjectMapper 전역 설정 클래스
 * - Java 8 날짜/시간(LocalDate, LocalDateTime 등) 직렬화/역직렬화를 위해 JavaTimeModule을 등록함
 * - Spring에서 공통으로 사용하는 ObjectMapper Bean 정의
 */

@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        return om;
    }
}
