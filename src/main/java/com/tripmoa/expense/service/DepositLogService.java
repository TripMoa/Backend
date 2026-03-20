package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.DepositLogCreateRequest;
import com.tripmoa.expense.dto.response.DepositLogResponse;
import com.tripmoa.expense.entity.DepositLog;
import com.tripmoa.expense.entity.Trip;
import com.tripmoa.expense.entity.TripMember;
import com.tripmoa.expense.enums.DepositLogStatus;
import com.tripmoa.expense.repository.DepositLogRepository;
import com.tripmoa.expense.repository.TripMemberRepository;
import com.tripmoa.expense.repository.TripRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class DepositLogService {

    private final TripService tripService;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final DepositLogRepository depositLogRepository;

    /**
     * 입금 로그 저장
     * - 권한: Trip 소유주 OR Trip 멤버
     * - 기본 상태: PENDING (승인 대기)
     * - 특정 Trip의 멤버에게 입금 내역 기록
     */
    public DepositLogResponse create(Long tripId, Long userId, DepositLogCreateRequest request) {
        tripService.assertOwnerOrMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));

        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        TripMember member = getValidMember(tripId, request.memberId());
        LocalDate depositDate = parseDepositDate(request.depositDate());

        DepositLog depositLog = DepositLog.builder()
                .trip(trip)
                .member(member)
                .createdBy(createdBy)
                .amount(request.amount())
                .depositDate(depositDate)
                .memo(request.memo())
                .depositStatus(DepositLogStatus.PENDING)
                .build();

        DepositLog saved = depositLogRepository.save(depositLog);
        return DepositLogResponse.from(saved);
    }

    /**
     * 입금 로그 삭제
     * - 권한: Trip 소유주 OR 입금 로그 등록자 OR 실제 입금한 대상 멤버
     * - 승인 상태 : Trip 소유주만 가능
     * - 승인 대기, 거절 상태 : 입금 로그 등록자 OR 실제 입금한 대상 멤버
     * - 다른 멤버 : 입금 로그 삭제 불가
     * - 다른 trip의 입금 로그는 삭제 불가
     */
    public void delete(Long tripId, Long depositLogId, Long userId) {
        tripService.assertOwnerOrMember(tripId, userId);

        DepositLog depositLog = depositLogRepository.findById(depositLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_LOG_NOT_FOUND));

        if (!depositLog.getTrip().getId().equals(tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 trip의 입금 로그가 아닙니다.");
        }

        boolean isOwner = depositLog.getTrip().getOwner().getId().equals(userId);
        boolean isCreator = depositLog.getCreatedBy().getId().equals(userId);
        boolean isDepositor = depositLog.getMember().getUser().getId().equals(userId);

        if (!isOwner && !isCreator && !isDepositor) {
            throw new BusinessException(ErrorCode.DEPOSIT_LOG_DELETE_FORBIDDEN, "삭제 권한이 없습니다.");
        }

        if (!isOwner && depositLog.getDepositStatus() == DepositLogStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.DEPOSIT_LOG_CONFIRMED_DELETE_NOT_ALLOWED, "승인된 입금 로그는 삭제할 수 없습니다.");
        }

        depositLogRepository.delete(depositLog);
    }

    /**
     * 입금 로그 승인
     * - 권한: Trip 소유주만
     */
    public DepositLogResponse confirm(Long tripId, Long depositLogId, Long userId) {
        tripService.assertOwner(tripId, userId);

        DepositLog depositLog = getValidDepositLog(tripId, depositLogId);
        depositLog.confirm();

        return DepositLogResponse.from(depositLog);
    }

    /**
     * 입금 로그 거절
     * - 권한: Trip 소유주만
     */
    public DepositLogResponse reject(Long tripId, Long depositLogId, Long userId) {
        tripService.assertOwner(tripId, userId);

        DepositLog depositLog = getValidDepositLog(tripId, depositLogId);
        depositLog.reject();

        return DepositLogResponse.from(depositLog);
    }

    private DepositLog getValidDepositLog(Long tripId, Long depositLogId) {
        DepositLog depositLog = depositLogRepository.findById(depositLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_LOG_NOT_FOUND));

        if (!depositLog.getTrip().getId().equals(tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 trip의 입금 로그가 아닙니다.");
        }

        return depositLog;
    }

    private TripMember getValidMember(Long tripId, Long memberId) {
        if (!tripMemberRepository.existsByIdAndTrip_Id(memberId, tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "memberId가 해당 trip의 멤버가 아닙니다.");
        }

        return tripMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "TripMember를 찾을 수 없습니다."));
    }

    private LocalDate parseDepositDate(String depositDate) {
        try {
            return LocalDate.parse(depositDate); // yyyy-MM-dd
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "입금 날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"
            );
        }
    }
}