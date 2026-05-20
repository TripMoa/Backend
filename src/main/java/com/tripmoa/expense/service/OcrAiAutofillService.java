package com.tripmoa.expense.service;

import com.tripmoa.ai.domain.AiClient;
import com.tripmoa.expense.dto.request.OcrAiAutofillResult;
import com.tripmoa.expense.dto.request.OcrLlmRequest;
import com.tripmoa.expense.dto.response.OcrInitialResponse;
import com.tripmoa.expense.dto.response.OcrLlmResponse;
import com.tripmoa.expense.enums.ExpenseCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OcrAiAutofillService {

    private final AiClient aiClient;

    // LLM 연결
    public OcrAiAutofillResult guessItemAndCategory(OcrInitialResponse base) {
        try {
            OcrLlmRequest request = new OcrLlmRequest(
                    base.storeName(),
                    base.menuName(),
                    base.paymentMethod(),
                    base.dateTime(),
                    base.totalAmount()
            );

            OcrLlmResponse response = aiClient.analyzeReceipt(request);

            if (response == null) {
                return fallback(base);
            }

            return new OcrAiAutofillResult(
                    safeItemMemo(response.itemMemo(), base),
                    response.category() == null ? ExpenseCategory.ETC : response.category()
            );

        } catch (Exception e) {
            return fallback(base);
        }
    }

    private OcrAiAutofillResult fallback(OcrInitialResponse base) {
        if (base.menuName() == null || base.menuName().isBlank()) {
            return new OcrAiAutofillResult("기타 지출", ExpenseCategory.ETC);
        }

        return new OcrAiAutofillResult(base.menuName(), ExpenseCategory.ETC);
    }

    private String safeItemMemo(String itemMemo, OcrInitialResponse base) {
        if (itemMemo != null && !itemMemo.isBlank()) {
            return itemMemo;
        }

        if (base.menuName() != null && !base.menuName().isBlank()) {
            return base.menuName();
        }

        return "기타 지출";
    }
}
