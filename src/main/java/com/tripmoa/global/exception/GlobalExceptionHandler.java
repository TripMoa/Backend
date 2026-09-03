package com.tripmoa.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GlobalExceptionHandler
 * - 애플리케이션 전역 예외 처리 클래스
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 의도적으로 던진 예외 (409/404 등)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest req) {
        ErrorCode ec = e.getErrorCode();
        return ResponseEntity
                .status(ec.getStatus())
                .body(new ErrorResponse(
                        ec.getCode(),
                        e.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        null
                ));
    }

    // Validation 실패 (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<ErrorResponse.FieldErrorItem> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldErrorItem(fe.getField(), defaultMessage(fe)))
                .toList();

        ErrorCode ec = ErrorCode.INVALID_REQUEST;
        return ResponseEntity
                .status(ec.getStatus())
                .body(new ErrorResponse(
                        ec.getCode(),
                        ec.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        errors
                ));
    }

    // ResponseStatusException (403/404/409)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e, HttpServletRequest req) {
        // 상태코드는 e.getStatusCode()로 유지
        String msg = (e.getReason() != null) ? e.getReason() : e.getMessage();

        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ErrorResponse(
                        e.getStatusCode().toString(),
                        msg,
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        null
                ));
    }

    // 스프링 6/부트 3+에서 일부는 ErrorResponseException으로도 들어옴
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponse(ErrorResponseException e, HttpServletRequest req) {
        String msg = (e.getBody() != null && e.getBody().getDetail() != null) ? e.getBody().getDetail() : e.getMessage();
        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ErrorResponse(
                        e.getStatusCode().toString(),
                        msg,
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        null
                ));
    }

    // 파일 용량 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.FILE_SIZE_EXCEEDED;
        return ResponseEntity
                .status(ec.getStatus())
                .body(new ErrorResponse(
                        ec.getCode(),
                        ec.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        null
                ));
    }

    // 그 외 전부 500 (로그는 ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest req) {
        log.error("UNEXPECTED_ERROR: path={}", req.getRequestURI(), e);

        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ec.getStatus())
                .body(new ErrorResponse(
                        ec.getCode(),
                        ec.getMessage(),
                        LocalDateTime.now(),
                        req.getRequestURI(),
                        null
                ));
    }

    private String defaultMessage(FieldError fe) {
        return (fe.getDefaultMessage() != null) ? fe.getDefaultMessage() : "invalid";
    }
}
