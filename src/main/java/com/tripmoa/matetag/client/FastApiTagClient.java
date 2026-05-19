package com.tripmoa.matetag.client;

import com.tripmoa.matetag.dto.FastApiTagRequest;
import com.tripmoa.matetag.dto.FastApiTagResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FastApiTagClient {

    private final RestClient restClient;

    public FastApiTagClient(@Value("${ai.server.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public FastApiTagResponse extractTags(FastApiTagRequest request) {
        return restClient.post()
                .uri("/mate/extract")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiTagResponse.class);
    }
}
