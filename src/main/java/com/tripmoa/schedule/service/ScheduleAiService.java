package com.tripmoa.schedule.service;

import com.tripmoa.ai.domain.AiClient;
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
}
