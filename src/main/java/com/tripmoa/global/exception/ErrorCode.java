package com.tripmoa.global.exception;

import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 * - 에러 코드와 메시지 관리
 * - code : [도메인]_[HTTP상태코드]_[일련번호] 형식 사용
 */

public enum ErrorCode {

    // ==============================
    // 공통 (Global)
    // ==============================

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


    // ==============================
    // Trip 도메인
    // ==============================

    // 존재하지 않는 Trip 조회 시
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP_404_001", "tripId에 해당하는 Trip이 없습니다."),

    // Trip에 해당하는 정산 설정이 없을 때
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_404_001", "tripId에 해당하는 정산 설정이 없습니다."),

    // Trip 소유주도 아니고 멤버도 아닌 경우
    TRIP_FORBIDDEN(HttpStatus.FORBIDDEN, "TRIP_403_001", "Trip 소유주 또는 멤버만 접근할 수 있습니다."),

    // Trip 소유주만 가능한 작업을 멤버가 시도한 경우
    TRIP_OWNER_REQUIRED(HttpStatus.FORBIDDEN, "TRIP_403_002", "Trip 소유주만 변경할 수 있습니다."),


    // ==============================
    // Settlement 정책 (409 충돌)
    // ==============================

    // POOL 모드인데 split 정책을 바꾸려는 경우
    SPLIT_POLICY_NOT_ALLOWED_IN_POOL(
            HttpStatus.CONFLICT,
            "SETTLEMENT_409_001",
            "POOL 모드에서는 분배 나머지 정책을 변경할 수 없습니다."
    ),

    // INDEPENDENT 모드인데 pool 정책을 바꾸려는 경우
    POOL_POLICY_NOT_ALLOWED_IN_INDEPENDENT(
            HttpStatus.CONFLICT,
            "SETTLEMENT_409_002",
            "INDEPENDENT 모드에서는 공금 정산 정책을 변경할 수 없습니다."
    ),


    // ==============================
    // OCR / 파일 업로드
    // ==============================

    // 파일이 비어있거나 형식이 잘못된 경우
    OCR_BAD_REQUEST(HttpStatus.BAD_REQUEST, "OCR_400_001", "OCR 요청이 올바르지 않습니다."),

    // 파일 크기가 10MB 초과한 경우
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "OCR_400_002", "파일 용량이 너무 큽니다. (최대 10MB)"),

    // 네이버 OCR API 호출 실패 / 내부 처리 오류
    OCR_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_500_001", "OCR 처리 중 오류가 발생했습니다."),


    // 일정 관련 (1000번대)
    INVALID_SCHEDULE(HttpStatus.BAD_REQUEST, "MATE_400_001", "유효하지 않은 여행 일정입니다"),
    START_DATE_BEFORE_NOW(HttpStatus.BAD_REQUEST, "MATE_400_002", "시작일은 현재 이후여야 합니다"),
    END_DATE_BEFORE_START(HttpStatus.BAD_REQUEST, "MATE_400_003", "종료일은 시작일 이후여야 합니다"),

    // 참가자 관련 (2000번대)
    INVALID_PARTICIPANT_INFO(HttpStatus.BAD_REQUEST, "MATE_400_004", "유효하지 않은 참가자 정보입니다"),
    CURRENT_PARTICIPANT_TOO_LOW(HttpStatus.BAD_REQUEST, "MATE_400_005", "현재 인원은 1명 이상이어야 합니다"),
    MAX_PARTICIPANT_TOO_LOW(HttpStatus.BAD_REQUEST, "MATE_400_006", "최대 인원은 2명 이상이어야 합니다"),
    CURRENT_EXCEEDS_MAX(HttpStatus.BAD_REQUEST, "MATE_400_007", "현재 인원이 최대 인원을 초과할 수 없습니다"),
    ALREADY_FULLY_BOOKED(HttpStatus.BAD_REQUEST, "MATE_400_008", "이미 모집 완료 상태입니다"),
    PARTICIPANT_FULL(HttpStatus.BAD_REQUEST, "MATE_400_009", "모집 인원이 마감되었습니다"),

    // 예산 관련 (3000번대)
    INVALID_BUDGET(HttpStatus.BAD_REQUEST, "MATE_400_010", "예산은 0 이상이어야 합니다"),

    // 게시글 관련 (4000번대)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "MATE_404_001", "게시글을 찾을 수 없습니다"),
    UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "MATE_403_001", "게시글 접근 권한이 없습니다"),

    // 신청서 관련 (5000번대)
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MATE_404_002", "신청서를 찾을 수 없습니다"),
    ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "MATE_400_011", "이미 신청한 게시글입니다"),
    CANNOT_APPLY_OWN_POST(HttpStatus.BAD_REQUEST, "MATE_400_012", "본인 게시글에는 신청할 수 없습니다"),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "MATE_400_013", "이미 처리된 신청서입니다"),
    UNAUTHORIZED_APPLICATION_ACCESS(HttpStatus.FORBIDDEN, "MATE_403_002", "신청서 접근 권한이 없습니다"),

    // ==============================
    // 서버 내부 오류
    // ==============================

    // 예기치 못한 서버 에러 (GlobalExceptionHandler fallback)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_001", "서버 내부 오류가 발생했습니다.");


    // ==============================
    // 필드
    // ==============================

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
