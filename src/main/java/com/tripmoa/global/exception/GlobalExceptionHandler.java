package com.tripmoa.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
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

    // ── 요청 자체가 잘못된 경우: 서버 오류(500)가 아니라 4xx로 알린다 ─────────────────────
    // (아래 예외들은 스프링이 던지는데, 따로 처리하지 않으면 마지막 handleUnexpected에 걸려 500이 된다)

    // JSON이 깨졌거나 본문이 없음
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST.getCode(),
                "요청 본문이 비어 있거나 올바른 JSON 형식이 아니에요.", req);
    }

    // 필수 요청 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST.getCode(),
                "필수 파라미터 '" + e.getParameterName() + "'이(가) 없어요.", req);
    }

    // 파라미터·경로 변수 타입 오류 (예: tripId=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST.getCode(),
                "'" + e.getName() + "' 값이 올바르지 않아요.", req);
    }

    // 허용되지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return clientError(HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED.toString(),
                "지원하지 않는 요청 방식이에요. (" + e.getMethod() + ")", req);
    }

    // 지원하지 않는 Content-Type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return clientError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE.toString(),
                "지원하지 않는 Content-Type이에요.", req);
    }

    // 없는 경로
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e, HttpServletRequest req) {
        return clientError(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.getCode(),
                "요청한 경로를 찾을 수 없어요.", req);
    }

    private ResponseEntity<ErrorResponse> clientError(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(code, message, LocalDateTime.now(), req.getRequestURI(), null));
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
