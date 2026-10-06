package com.tripmoa.schedule.service;

import com.tripmoa.ai.domain.AiClient;
import com.tripmoa.ai.dto.AiEstimateResponse;
import com.tripmoa.ai.dto.AiScheduleRequest;
import com.tripmoa.ai.dto.AiScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI 호출 담당
 */
@Service
@RequiredArgsConstructor
public class ScheduleAiService {

    private final AiClient aiClient;

    public AiScheduleResponse response(AiScheduleRequest request) {
        return aiClient.generate(request);
    }

    // 순서가 정해진 하루의 시각 재계산
    public java.util.Map<String, Object> recomputeDay(java.util.Map<String, Object> body) {
        return aiClient.recomputeDay(body);
    }

    // 구간 실시간 대중교통 경로 (저장하지 않고 그대로 돌려준다)
    public java.util.Map<String, Object> transit(java.util.Map<String, Object> body) {
        return aiClient.transit(body);
    }

    public AiEstimateResponse estimate(AiScheduleRequest request) {
        return aiClient.estimate(request);
    }
}
