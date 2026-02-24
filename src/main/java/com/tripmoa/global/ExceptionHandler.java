package com.tripmoa.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

// 외부 API(네이버 OCR) 호출 시 발생할 수 있는 오류나 파일 업로드 관련 예외 처리

@RestControllerAdvice
public class ExceptionHandler {

    // OCR 호출 중 발생하는 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "OCR_PROCESS_ERROR");
        // e.getMessage()에 네이버가 준 "bad request" 상세 내용이 담기도록 유도
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 파일 용량 초과 예외 처리
    @org.springframework.web.bind.annotation.ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "FILE_SIZE_EXCEEDED");
        response.put("message", "파일 용량이 너무 큽니다. (최대 10MB)");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
