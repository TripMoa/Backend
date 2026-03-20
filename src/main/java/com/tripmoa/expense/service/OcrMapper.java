package com.tripmoa.expense.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmoa.expense.dto.response.OcrAutofillResponse;
import com.tripmoa.expense.dto.response.OcrInitialResponse;
import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OcrMapper {

    private final ObjectMapper objectMapper;

    public OcrInitialResponse toAutofillResponse(Map<String, Object> ocrRaw) {
        JsonNode root = objectMapper.valueToTree(ocrRaw);

        JsonNode fields = root.path("images").path(0).path("fields");

        // fields가 없으면 빈값 처리
        if (!fields.isArray()) {
            return new OcrInitialResponse(null, null, null, null, null);
        }

        String menuName = null;
        String paymentMethod = null;
        String storeName = null;
        String dateTime = null;
        Integer totalAmount = null;

        for (JsonNode f : fields) {
            String name = f.path("name").asText("");
            String inferText = normalizeText(f.path("inferText").asText(null));

            switch (name) {
                case "menuName" -> menuName = inferText;
                case "paymentMethod" -> paymentMethod = inferText;
                case "storeName" -> storeName = inferText;
                case "totalAmount" -> totalAmount = parseAmount(inferText);
                case "dateTime" -> dateTime = parseDateTime(f);
            }
        }

        return new OcrInitialResponse(storeName, menuName, paymentMethod, dateTime, totalAmount);
    }

    private String parseDateTime(JsonNode fieldNode) {
        // inferText에 "2026-01-30 17:15:29"처럼 이미 합쳐져 있으면 그걸 우선
        String infer = normalizeText(fieldNode.path("inferText").asText(null));
        if (infer != null && infer.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return normalizeDateTime(infer);
        }

        // subFields로 "2026-01-30" + "17:15:29" 이런 식이면 합치기
        JsonNode sub = fieldNode.path("subFields");
        if (sub.isArray() && sub.size() >= 1) {
            String date = normalizeText(sub.path(0).path("inferText").asText(null));
            String time = sub.size() >= 2 ? normalizeText(sub.path(1).path("inferText").asText(null)) : null;
            if (date != null && time != null) return normalizeDateTime(date + " " + time);
            if (date != null) return date;
        }
        return null;
    }

    private String normalizeText(String s) {
        if (s == null) return null;
        // "17: 15: 29" 처럼 OCR이 공백을 끼우는 경우가 많아서 공백 정리
        return s.replaceAll("\\s+", " ").trim();
    }

    private String normalizeDateTime(String s) {
        // "17: 15: 29" → "17:15:29"
        return s.replaceAll("\\s*:\\s*", ":").trim();
    }

    private Integer parseAmount(String s) {
        if (s == null) return null;
        // "5,900원" / "₩ 5,900" / "5900" 모두 처리
        String onlyNum = s.replaceAll("[^0-9]", "");
        if (onlyNum.isEmpty()) return null;
        try {
            return Integer.parseInt(onlyNum);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 임시 자동채움
    public OcrAutofillResponse toAutofillResult(Map<String, Object> ocrRaw) {
        OcrInitialResponse base = toAutofillResponse(ocrRaw);

        return new OcrAutofillResponse(
                base.storeName(),
                base.menuName(),                       // menuName → itemMemo
                ExpenseCategory.ETC,                   // 고정
                mapPayMethod(base.paymentMethod()),    // 간단 매핑
                base.dateTime(),                       // dateTime → paidAt
                base.totalAmount()
        );
    }

    // PayMethod 간단 매핑
    private PayMethod mapPayMethod(String paymentMethodText) {
        if (paymentMethodText == null) return PayMethod.CARD;

        String t = paymentMethodText.toLowerCase();
        if (t.contains("현금")) return PayMethod.CASH;
        if (t.contains("qr") || t.contains("페이") || t.contains("pay")) return PayMethod.QR;
        if (t.contains("카드") || t.contains("credit") || t.contains("check")) return PayMethod.CARD;

        return PayMethod.CARD;
    }
}
