package com.tripmoa.global.util;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

/* 금칙어 필터
 - 게시글 제목, 내용, 댓글 등에 금칙어 포함 여부 검사 */

@Component
public class BadWordFilter {

    // 금칙어 목록
    private static final List<String> BANNED_WORDS = Arrays.asList(
            "욕설1", "욕설2"  // 금칙어 추가 가능
    );

    // 입력 텍스트에 금칙어가 포함되어 있는지 검사
    public boolean containsBadWord(String text) {

        // null 방지
        if (text == null) return false;

        // 입력 텍스트 로그 출력
        System.out.println("입력 텍스트: " + text);

        // 금칙어 포함 여부 검사
        boolean result = BANNED_WORDS.stream()
                .anyMatch(word -> text.contains(word));

        // 검사 결과 로그 출력
        System.out.println("금칙어 감지 결과: " + result);

        return result;
    }
}