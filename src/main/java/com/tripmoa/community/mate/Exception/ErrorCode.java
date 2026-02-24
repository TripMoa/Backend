package com.tripmoa.community.mate.Exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 일정 관련 (1000번대)
    INVALID_SCHEDULE(HttpStatus.BAD_REQUEST, "M1001", "유효하지 않은 여행 일정입니다"),
    START_DATE_BEFORE_NOW(HttpStatus.BAD_REQUEST, "M1002", "시작일은 현재 이후여야 합니다"),
    END_DATE_BEFORE_START(HttpStatus.BAD_REQUEST, "M1003", "종료일은 시작일 이후여야 합니다"),

    // 참가자 관련 (2000번대)
    INVALID_PARTICIPANT_INFO(HttpStatus.BAD_REQUEST, "M2001", "유효하지 않은 참가자 정보입니다"),
    CURRENT_PARTICIPANT_TOO_LOW(HttpStatus.BAD_REQUEST, "M2002", "현재 인원은 1명 이상이어야 합니다"),
    MAX_PARTICIPANT_TOO_LOW(HttpStatus.BAD_REQUEST, "M2003", "최대 인원은 2명 이상이어야 합니다"),
    CURRENT_EXCEEDS_MAX(HttpStatus.BAD_REQUEST, "M2004", "현재 인원이 최대 인원을 초과할 수 없습니다"),
    ALREADY_FULLY_BOOKED(HttpStatus.BAD_REQUEST, "M2005", "이미 모집 완료 상태입니다"),
    PARTICIPANT_FULL(HttpStatus.BAD_REQUEST, "M2006", "모집 인원이 마감되었습니다"),

    // 예산 관련 (3000번대)
    INVALID_BUDGET(HttpStatus.BAD_REQUEST, "M3001", "예산은 0 이상이어야 합니다"),

    // 게시글 관련 (4000번대)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "M4001", "게시글을 찾을 수 없습니다"),
    UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "M4002", "게시글 접근 권한이 없습니다"),

    // 신청서 관련 (5000번대)
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "M5001", "신청서를 찾을 수 없습니다"),
    ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "M5002", "이미 신청한 게시글입니다"),
    CANNOT_APPLY_OWN_POST(HttpStatus.BAD_REQUEST, "M5003", "본인 게시글에는 신청할 수 없습니다"),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "M5004", "이미 처리된 신청서입니다"),
    UNAUTHORIZED_APPLICATION_ACCESS(HttpStatus.FORBIDDEN, "M5005", "신청서 접근 권한이 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
