package com.tripmoa.matetag.client;

import com.tripmoa.matetag.dto.FastApiTagRequest;
import com.tripmoa.matetag.dto.FastApiTagResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class FastApiTagClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FastApiTagClient(@Value("${ai.server.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public FastApiTagResponse extractTags(FastApiTagRequest request) {
        log.info("FastAPI 요청: {}", request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FastApiTagRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<FastApiTagResponse> response = restTemplate.postForEntity(
                baseUrl + "/mate/extract", entity, FastApiTagResponse.class);

        return response.getBody();
    }

//    private final RestClient restClient;
//
//    public FastApiTagClient(@Value("${ai.server.url}") String baseUrl) {
//        this.restClient = RestClient.builder()
//                .baseUrl(baseUrl)
//                .build();
//    }
//
//    public FastApiTagResponse extractTags(FastApiTagRequest request) {
//        return restClient.post()
//                .uri("/mate/extract")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(request)
//                .retrieve()
//                .body(FastApiTagResponse.class);
//    }
}
