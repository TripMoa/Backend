package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.BudgetAmountUpdateRequest;
import com.tripmoa.expense.dto.request.PaymentModeChangeRequest;
import com.tripmoa.expense.dto.request.PoolBalancePolicyChangeRequest;
import com.tripmoa.expense.dto.request.SplitRemainderPolicyChangeRequest;
import com.tripmoa.expense.dto.response.SettlementSettingResponse;
import com.tripmoa.expense.entity.SettlementSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 정산 설정 저장/조회
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementSettingService {

    private final TripService tripService;

    /**
     * 정산 설정 조회
     * - 권한: Trip 소유주 OR Trip 멤버
     */
    @Transactional(readOnly = true)
    public SettlementSettingResponse getSetting(Long tripId, Long userId) {
        tripService.assertOwnerOrMember(tripId, userId);

        SettlementSetting setting = tripService.getSettingOr404(tripId);
        return toResponse(setting, tripId);
    }

    /**
     * payment mode 변경
     * - 권한: Trip 소유주만
     * - 정책:
     *   - 기존 지출 원본은 변경하지 않음
     *   - 이후 화면의 정산 결과만 현재 설정 기준으로 재계산
     */
    public SettlementSettingResponse changePaymentMode(Long tripId, Long userId, PaymentModeChangeRequest request) {
        tripService.assertOwner(tripId, userId);

        SettlementSetting setting = tripService.getSettingOr404(tripId);
        setting.changePaymentMode(request.getPaymentMode());

        return toResponse(setting, tripId);
    }

    /**
     * split remainder policy 변경
     * - 권한: Trip 소유주만
     * - 정책:
     *   - 모든 mode에서 변경 가능
     *   - 새 preview / 새 지출 저장부터 적용
     *   - 기존 ExpenseSplit 원본은 변경하지 않음
     */
    public SettlementSettingResponse changeSplitRemainderPolicy(
            Long tripId,
            Long userId,
            SplitRemainderPolicyChangeRequest request
    ) {
        tripService.assertOwner(tripId, userId);

        SettlementSetting setting = tripService.getSettingOr404(tripId);
        setting.changeSplitRemainderPolicy(request.getSplitRemainderPolicy());

        return toResponse(setting, tripId);
    }

    /**
     * pool balance policy 변경
     * - 권한: Trip 소유주만
     * - 정책:
     *   - 모든 mode에서 변경 가능
     *   - 현재 mode에서 필요할 때만 summary 계산에 사용
     */
    public SettlementSettingResponse changePoolBalancePolicy(
            Long tripId,
            Long userId,
            PoolBalancePolicyChangeRequest request
    ) {
        tripService.assertOwner(tripId, userId);

        SettlementSetting setting = tripService.getSettingOr404(tripId);
        setting.changePoolBalancePolicy(request.getPoolBalancePolicy());

        return toResponse(setting, tripId);
    }

    /**
     * budget 변경
     * - 권한: Trip 소유주만
     */
    public SettlementSettingResponse updateBudget(Long tripId, Long userId, BudgetAmountUpdateRequest request) {
        tripService.assertOwner(tripId, userId);

        SettlementSetting setting = tripService.getSettingOr404(tripId);
        setting.changeBudgetAmount(request.getBudgetAmount());

        return toResponse(setting, tripId);
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