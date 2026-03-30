package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.*;
import com.tripmoa.expense.dto.response.SettlementSettingResponse;
import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 정산 설정 저장/조회
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementSettingService {

    private final TripPermissionService tripPermissionService;
    private final TripMemberRepository tripMemberRepository;

    /**
     * 정산 설정 조회
     * - 권한: Trip 소유주 OR Trip 멤버
     */
    @Transactional(readOnly = true)
    public SettlementSettingResponse getSetting(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        SettlementSetting setting = tripPermissionService.getSettingOr404(tripId);
        return toResponse(setting, tripId);
    }

    /**
     * 설정 일괄 저장 (설정 적용 버튼)
     * - 권한: Trip 소유주
     * - 정책: 예산은 멤버 수로 나누어떨어져야 함
     */
    public SettlementSettingResponse updateAll(Long tripId, Long userId, SettlementUpdateAllRequest request) {
        tripPermissionService.assertOwner(tripId, userId);

        // 예산 유효성 검사 (멤버 수로 나누어떨어지는지 확인)
        validateBudgetAmount(tripId, request.getBudgetAmount());

        // 엔티티 조회 및 업데이트
        SettlementSetting setting = tripPermissionService.getSettingOr404(tripId);
        setting.updateAll(
                request.getPaymentMode(),
                request.getSplitRemainderPolicy(),
                request.getPoolBalancePolicy(),
                request.getBudgetAmount()
        );

        return toResponse(setting, tripId);
    }

    private void validateBudgetAmount(Long tripId, int budgetAmount) {
        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);
        int memberCount = members.size();

        if (memberCount > 0 && budgetAmount % memberCount != 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    String.format("예산은 멤버 수(%d명)로 딱 나누어떨어져야 합니다. (현재 나머지: %d원)",
                            memberCount, budgetAmount % memberCount)
            );
        }
    }

    /**
     * 응답 DTO 변환
     * - 설정값은 null 정리/보정 없이 그대로 반환
     * - 어떤 정책을 실제로 사용할지는 preview/summary 서비스에서 mode 기준으로 분기
     */
    private SettlementSettingResponse toResponse(SettlementSetting setting, Long tripId) {
        return SettlementSettingResponse.builder()
                .tripId(tripId)
                .paymentMode(setting.getPaymentMode())
                .splitRemainderPolicy(setting.getSplitRemainderPolicy())
                .poolBalancePolicy(setting.getPoolBalancePolicy())
                .budgetAmount(setting.getBudgetAmount())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}