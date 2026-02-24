package com.tripmoa.expense;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExService {

    @Value("${naver.ocr.invoke-url}")
    private String invokeUrl;

    @Value("${naver.ocr.secret-key}")
    private String secretKey;

    @Value("${naver.ocr.api-gateway-key-id:}")
    private String apiGatewayKeyId;

    @Value("${naver.ocr.api-gateway-key:}")
    private String apiGatewayKey;

    @Value("${naver.ocr.domain-id:}")
    private String domainId;

    @Value("${naver.ocr.signature:}")
    private String signature;

    @Value("${naver.ocr.path:}")
    private String path;

    private final RestTemplate restTemplate;

    public Map<String, Object> callOcrApi(MultipartFile file) {
        try {
            // 인코딩 이슈 방지
            DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
            uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
            restTemplate.setUriTemplateHandler(uriFactory);

            // RestTemplate 기본 에러 핸들러가 body를 숨기는 경우가 있어 직접 로깅
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

            // 최종 URL 만들기
            String url = buildFinalUrl();

            // 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // OCR Secret
            headers.set("X-OCR-SECRET", secretKey);

            // Usage Plan에서 API Key 강제를 걸었거나, 키가 필요한 상품이면 추가
            if (!isBlank(apiGatewayKeyId) && !isBlank(apiGatewayKey)) {
                headers.set("X-NCP-APIGW-API-KEY-ID", apiGatewayKeyId);
                headers.set("X-NCP-APIGW-API-KEY", apiGatewayKey);
            }

            // multipart body: file + message(JSON 문자열)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // file 파트
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() == null ? "upload.jpg" : file.getOriginalFilename();
                }
            });

            // message 파트 (템플릿 OCR 공통 형식)
            String ext = getExt(file.getOriginalFilename());
            String messageJson = buildMessageJson(ext);

            body.add("message", messageJson);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 호출
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            return (Map<String, Object>) response.getBody();

        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("네이버 OCR 에러: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("OCR 호출 중 오류 발생: " + e.getMessage());
        }
    }

    private String buildFinalUrl() {
        if (isBlank(domainId) || isBlank(signature) || isBlank(path)) {
            return invokeUrl;
        }

        String base = invokeUrl;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        String p = path;
        if (p.startsWith("/")) p = p.substring(1);

        return base + "/" + domainId + "/" + signature + "/" + p;
    }

    private String buildMessageJson(String format) {

        return """
        {
          "version": "V2",
          "requestId": "%s",
          "timestamp": %d,
          "images": [
            {
              "format": "%s",
              "name": "receipt_test"
            }
          ]
        }
        """.formatted(UUID.randomUUID(), System.currentTimeMillis(), format);
    }

    private String getExt(String filename) {
        if (filename == null) return "jpg";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return "jpg";
        String ext = filename.substring(idx + 1).toLowerCase();
        if ("jpeg".equals(ext)) return "jpg";
        return ext;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}