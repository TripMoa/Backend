package com.tripmoa.ai.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.ai.dto.AiScheduleResponse;
import com.tripmoa.expense.dto.request.OcrLlmRequest;
import com.tripmoa.expense.dto.response.OcrLlmResponse;
import com.tripmoa.place.dto.PlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.SocketTimeoutException;

/**
 * AiClient
 * - Python AI 서버와 통신하는 전용 클래스
 * - 일정 생성 + 장소 검색 두 가지 역할
 *
 * Python(FastAPI)이 던지는 400 등의 구체적인 사유({"detail": "..."})를
 * 그대로 살려서 프론트에 전달한다 — 안 그러면 전부 범용 500으로 뭉개짐.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // AI 일정 생성
    public AiScheduleResponse generate(AiScheduleRequest request) {
        String url = aiServerUrl + "/schedule/generate";
        try {
            return restTemplate.postForObject(url, request, AiScheduleResponse.class);
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(), extractDetail(e), e);
        } catch (ResourceAccessException e) {
            throw toTimeoutOrUnavailable(e);
        }
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
        try {
            return restTemplate.getForObject(uri, PlaceSearchResponse.class);
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(), extractDetail(e), e);
        } catch (ResourceAccessException e) {
            throw toTimeoutOrUnavailable(e);
        }
    }

    // OCR 영수증 LLM 분석
    public OcrLlmResponse analyzeReceipt(OcrLlmRequest request) {
        String url = aiServerUrl + "/ocr/analyze";
        return restTemplate.postForObject(url, request, OcrLlmResponse.class);
    }

    // Python(FastAPI)의 HTTPException 응답은 {"detail": "..."} 형태 — 최대한 그 사유를 그대로 전달
    private String extractDetail(HttpStatusCodeException e) {
        try {
            JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
            JsonNode detail = node.get("detail");
            if (detail != null && detail.isTextual()) return detail.asText();
        } catch (Exception parseError) {
            log.warn("AI 서버 에러 응답 파싱 실패: {}", e.getResponseBodyAsString());
        }
        return "AI 서버 요청이 실패했습니다.";
    }

    private ResponseStatusException toTimeoutOrUnavailable(ResourceAccessException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
            return new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "AI 서버 응답이 너무 오래 걸립니다. 잠시 후 다시 시도해주세요.",
                    e
            );
        }
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.",
                e
        );
    }
}