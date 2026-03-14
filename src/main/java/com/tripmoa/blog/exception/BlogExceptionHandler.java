package com.tripmoa.blog.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/* 블로그 관련 예외 처리 핸들러
 - Blog 패키지에서 발생하는 예외를 공통 처리 (금칙어 필터에서 사용) */

@RestControllerAdvice(basePackages = "com.tripmoa.blog")
public class BlogExceptionHandler {

    // 잘못된 요청 처리 (IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException e) {

        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }
}