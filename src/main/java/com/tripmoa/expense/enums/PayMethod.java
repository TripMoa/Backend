package com.tripmoa.expense.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

// 결제 방식
public enum PayMethod {
    CARD, QR, CASH;

    @JsonCreator
    public static PayMethod fromText(String text) {
        if (text == null) return CARD;

        String t = text.toLowerCase();

        if (t.contains("현금")) return CASH;
        if (t.contains("qr") || t.contains("페이") || t.contains("pay")) return QR;
        if (t.contains("카드") || t.contains("credit") || t.contains("check")) return CARD;

        return CARD;
    }
}
