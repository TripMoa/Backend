package com.tripmoa.global.exception;

import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 * - 에러 코드와 메시지 관리
 * - code : [도메인]_[HTTP상태코드]_[일련번호] 형식 사용
 */

public enum ErrorCode {

    // ====== 공통 (Global) ======

    // 400 - 요청 파라미터/바디 값이 잘못된 경우
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400_001", "요청 값이 올바르지 않습니다."),

    // 401 - 로그인(인증) 안 된 상태
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401_001", "인증이 필요합니다."),

    // 403 - 로그인은 했지만 권한이 없는 경우
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403_001", "권한이 없습니다."),

    // 404 - 리소스 자체가 존재하지 않는 경우
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_001", "대상을 찾을 수 없습니다."),

    // 409 - 현재 상태와 충돌 (예: 정책 변경 불가 등)
    CONFLICT(HttpStatus.CONFLICT, "COMMON_409_001", "요청이 현재 상태와 충돌합니다."),


    // ====== User 도메인 ======

    // 존재하지 않는 User 조회 시
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_001", "userId에 해당하는 사용자가 없습니다."),


    // ====== Trip 도메인 ======

    // 존재하지 않는 Trip 조회 시
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP_404_001", "tripId에 해당하는 Trip이 없습니다."),

    // Trip에 해당하는 정산 설정이 없을 때
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_404_001", "tripId에 해당하는 정산 설정이 없습니다."),

    // Trip 소유주도 아니고 멤버도 아닌 경우
    TRIP_FORBIDDEN(HttpStatus.FORBIDDEN, "TRIP_403_001", "Trip 소유주 또는 멤버만 접근할 수 있습니다."),

    // Trip 소유주만 가능한 작업을 멤버가 시도한 경우
    TRIP_OWNER_REQUIRED(HttpStatus.FORBIDDEN, "TRIP_403_002", "Trip 소유주만 변경할 수 있습니다."),


    // ====== Expense 도메인 ======

    // 존재하지 않는 Expense 조회 시
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE_404_001", "expenseId에 해당하는 영수증이 없습니다."),


    // ====== Deposit 도메인 ======

    // 입금 로그 삭제 권한이 없는 경우
    DEPOSIT_LOG_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "DEPOSIT_403_001", "입금 로그를 삭제할 권한이 없습니다."),

    // 승인된 입금 로그는 소유주만 삭제 가능한 경우
    DEPOSIT_LOG_CONFIRMED_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "DEPOSIT_409_001", "승인된 입금 로그는 소유주만 삭제할 수 있습니다."),

    // 존재하지 않는 입금 로그 조회 시
    DEPOSIT_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPOSIT_404_001", "depositLogId에 해당하는 입금 로그가 없습니다."),


    // ====== Settlement 정책 (409 충돌) ======

    // POOL 모드인데 split 정책을 바꾸려는 경우
    SPLIT_POLICY_NOT_ALLOWED_IN_POOL(HttpStatus.CONFLICT, "SETTLEMENT_409_001", "POOL 모드에서는 분배 나머지 정책을 변경할 수 없습니다."),

    // INDEPENDENT 모드인데 pool 정책을 바꾸려는 경우
    POOL_POLICY_NOT_ALLOWED_IN_INDEPENDENT( HttpStatus.CONFLICT, "SETTLEMENT_409_002", "INDEPENDENT 모드에서는 공금 정산 정책을 변경할 수 없습니다."),


    // ====== OCR / 파일 업로드 ======

    // 파일이 비어있거나 형식이 잘못된 경우
    OCR_BAD_REQUEST(HttpStatus.BAD_REQUEST, "OCR_400_001", "OCR 요청이 올바르지 않습니다."),

    // 파일 크기가 10MB 초과한 경우
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "OCR_400_002", "파일 용량이 너무 큽니다. (최대 10MB)"),

    // 네이버 OCR API 호출 실패 / 내부 처리 오류
    OCR_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_500_001", "OCR 처리 중 오류가 발생했습니다."),


    // ====== 서버 내부 오류 ======

    // 예기치 못한 서버 에러 (GlobalExceptionHandler fallback)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_001", "서버 내부 오류가 발생했습니다.");


    // ============================
    // 필드
    // ============================

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
