package com.tripmoa.trip.service;

import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.expense.repository.SettlementSettingRepository;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.repository.TripRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 헬퍼 메서드
 * - 존재 여부 체크 + 권한 체크
 */

@Service
@RequiredArgsConstructor
public class TripPermissionService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final SettlementSettingRepository settingRepository;

    // Trip이 존재하는지 확인하고 없으면 바로 404 에러 발생
    public Trip getTripOr404(Long tripId) {
        return tripRepository.findByIdAndStatus(tripId, TripStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));
    }

    // 해당 Trip의 정산 설정이 있는지 확인하고 없으면 404 에러 발생
    public SettlementSetting getSettingOr404(Long tripId) {
        return settingRepository.findByTrip_Id(tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));
    }

    // 조회(GET) 권한 체크 : Trip 소유주거나 멤버가 아니면 403 Forbidden 에러 발생
    public void assertOwnerOrMember(Long tripId, Long userId) {
        Trip trip = getTripOr404(tripId);

        boolean isOwner = trip.getOwner().getId().equals(userId);
        boolean isMember = tripMemberRepository.existsByTrip_IdAndUser_Id(tripId, userId);

        if (!isOwner && !isMember) {
            throw new BusinessException(ErrorCode.TRIP_FORBIDDEN);
        }
    }

    // 변경(PUT) 권한 체크 : 오직 Trip 소유주만 가능
    public void assertOwner(Long tripId, Long userId) {
        Trip trip = getTripOr404(tripId);

        if (!trip.getOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.TRIP_OWNER_REQUIRED);
        }
    }

}
