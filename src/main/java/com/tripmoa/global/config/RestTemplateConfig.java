package com.tripmoa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateConfig
 *
 * - 외부 API 호출을 위한 RestTemplate Bean 설정
 * - 연결 타임아웃(5초) 및 응답 타임아웃(30초) 설정
 * - OCR, 외부 LLM 등 외부 서비스 호출 시 사용
 */

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(30000);
        return new RestTemplate(f);
    }
}