package com.tripmoa.ai.domain;

import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.ai.dto.AiScheduleResponse;
import com.tripmoa.expense.dto.request.OcrLlmRequest;
import com.tripmoa.expense.dto.response.OcrLlmResponse;
import com.tripmoa.place.dto.PlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * AiClient
 * - Python AI 서버와 통신하는 전용 클래스
 * - 일정 생성 + 장소 검색 두 가지 역할
 */
@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // AI 일정 생성
    public AiScheduleResponse generate(AiScheduleRequest request) {
        String url = aiServerUrl + "/schedule/generate";
        return restTemplate.postForObject(url, request, AiScheduleResponse.class);
    }

    // 장소 검색 프록시
    public PlaceSearchResponse search(String query, int display) {
        // Spring이 @RequestParam으로 이미 디코딩한 query를 UriComponentsBuilder로 다시 인코딩
        java.net.URI uri = UriComponentsBuilder
                .fromUriString(aiServerUrl + "/schedule/search")
                .queryParam("query", query)
                .queryParam("display", display)
                .build()
                .encode()
                .toUri();
        return restTemplate.getForObject(uri, PlaceSearchResponse.class);
    }

    // OCR 영수증 LLM 분석
    public OcrLlmResponse analyzeReceipt(OcrLlmRequest request) {
        String url = aiServerUrl + "/ocr/analyze";
        return restTemplate.postForObject(url, request, OcrLlmResponse.class);
    }

}