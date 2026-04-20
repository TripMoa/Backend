package com.tripmoa.global.util;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/* 금칙어 필터
 - 게시글 제목, 내용, 댓글 등에 금칙어 포함 여부 검사 */

@Slf4j
@Component
public class BadWordFilter {

    // 금칙어 목록
    private static final List<String> BANNED_WORDS = Arrays.asList(
            "ㅅㅂ", "시발", "씨발",
            "ㅂㅅ", "병신", "ㅈㄴ", "존나",
            "개새끼", "개새", "미친", "미친놈",
            "미친년", "ㅁㅊ", "꺼져", "닥쳐"
    );

    // 입력 텍스트에 금칙어가 포함되어 있는지 검사
    public boolean containsBadWord(String text) {

        if (text == null) return false;

        String lowerText = text.toLowerCase();

        boolean result = BANNED_WORDS.stream()
                .anyMatch(word -> lowerText.contains(word.toLowerCase()));

        if (result) {
            log.warn("금칙어 감지됨");
        }

        return result;
    }
}