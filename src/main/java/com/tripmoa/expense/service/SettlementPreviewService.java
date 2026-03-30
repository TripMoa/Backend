package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.ExpensePreviewManualSplitRequest;
import com.tripmoa.expense.dto.request.ExpensePreviewRequest;
import com.tripmoa.expense.dto.response.ExpensePreviewResponse;
import com.tripmoa.expense.dto.response.ExpensePreviewSplitResponse;
import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.SplitMode;
import com.tripmoa.expense.enums.SplitRemainderPolicy;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// 지출 저장 전 1건 분할 미리보기 계산

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementPreviewService {

    private static final SplitRemainderPolicy DEFAULT_SPLIT_POLICY = SplitRemainderPolicy.TO_PAYER;

    private final TripPermissionService tripPermissionService;
    private final TripMemberRepository tripMemberRepository;

    public ExpensePreviewResponse preview(Long tripId, Long userId, ExpensePreviewRequest request) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        SettlementSetting setting = tripPermissionService.getSettingOr404(tripId);

        // payer 검증
        TripMember payer = getTripMemberOrThrow(tripId, request.payerMemberId());

        boolean isShared = Boolean.TRUE.equals(request.isShared());
        boolean autoIncludePayer = request.autoIncludePayer() == null || request.autoIncludePayer();

        // POOL 모드에서는 개인 지출 불가
        if (setting.getPaymentMode() == PaymentMode.POOL && !isShared) {
            return ExpensePreviewResponse.builder()
                    .paymentMode(setting.getPaymentMode())
                    .splitMode(request.splitMode())
                    .isShared(false)
                    .totalAmount(request.totalAmount())
                    .splitTotalAmount(0)
                    .diffAmount(request.totalAmount())
                    .canSave(false)
                    .message("모임 통장 모드에서는 개인 지출을 등록할 수 없습니다.")
                    .splits(List.of())
                    .build();
        }

        List<ExpensePreviewSplitResponse> splits =
                buildPreviewSplits(tripId, payer, request, autoIncludePayer, setting);

        int splitTotal = splits.stream()
                .mapToInt(ExpensePreviewSplitResponse::amount)
                .sum();

        return ExpensePreviewResponse.builder()
                .paymentMode(setting.getPaymentMode())
                .splitMode(request.splitMode())
                .isShared(isShared)
                .totalAmount(request.totalAmount())
                .splitTotalAmount(splitTotal)
                .diffAmount(request.totalAmount() - splitTotal)
                .canSave(splitTotal == request.totalAmount())
                .message(splitTotal == request.totalAmount() ? "미리보기 계산이 완료되었습니다." : "분할 합계가 총 금액과 일치하지 않습니다.")
                .splits(splits)
                .build();
    }

    private List<ExpensePreviewSplitResponse> buildPreviewSplits(
            Long tripId,
            TripMember payer,
            ExpensePreviewRequest request,
            boolean autoIncludePayer,
            SettlementSetting setting
    ) {
        if (request.splitMode() == SplitMode.EQUAL) {
            return buildEqualSplits(tripId, payer, request.totalAmount(), request.joinedMemberIds(), autoIncludePayer, setting);
        }

        if (request.splitMode() == SplitMode.AMOUNT) {
            return buildManualSplits(tripId, payer, request.totalAmount(), request.manualSplits(), autoIncludePayer);
        }

        throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 splitMode 입니다.");
    }

    private List<ExpensePreviewSplitResponse> buildEqualSplits(
            Long tripId,
            TripMember payer,
            int totalAmount,
            List<Long> joinedMemberIds,
            boolean autoIncludePayer,
            SettlementSetting setting
    ) {
        if (joinedMemberIds == null || joinedMemberIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "균등 분할에는 참여자 목록이 필요합니다.");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(joinedMemberIds);

        if (autoIncludePayer) {
            uniqueIds.add(payer.getId());
        }

        List<TripMember> members = loadAndValidateMembers(tripId, uniqueIds);

        if (members.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "분할 대상 참여자가 없습니다.");
        }

        // sortOrder 기준 정렬
        members.sort(Comparator.comparingInt(TripMember::getSortOrder));

        int size = members.size();
        int baseAmount = totalAmount / size;
        int remainder = totalAmount % size;

        Map<Long, Integer> amounts = new LinkedHashMap<>();
        for (TripMember member : members) {
            amounts.put(member.getId(), baseAmount);
        }

        SplitRemainderPolicy policy = resolveSplitPolicy(setting);

        applyRemainder(policy, amounts, members, payer.getId(), remainder);

        return members.stream()
                .map(member -> ExpensePreviewSplitResponse.builder()
                        .memberId(member.getId())
                        .nickname(member.getNickname())
                        .amount(amounts.get(member.getId()))
                        .payer(member.getId().equals(payer.getId()))
                        .build())
                .toList();
    }

    private List<ExpensePreviewSplitResponse> buildManualSplits(
            Long tripId,
            TripMember payer,
            int totalAmount,
            List<ExpensePreviewManualSplitRequest> manualSplits,
            boolean autoIncludePayer
    ) {
        if (manualSplits == null || manualSplits.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "직접 입력 분할에는 manualSplits가 필요합니다.");
        }

        Map<Long, Integer> amountMap = new LinkedHashMap<>();
        for (ExpensePreviewManualSplitRequest split : manualSplits) {
            if (split == null || split.memberId() == null || split.amount() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "manualSplits 항목이 올바르지 않습니다.");
            }

            if (split.amount() < 0) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "분할 금액은 0 이상이어야 합니다.");
            }

            if (amountMap.put(split.memberId(), split.amount()) != null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "manualSplits에 중복 memberId가 포함되어 있습니다.");
            }
        }

        if (autoIncludePayer && !amountMap.containsKey(payer.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "autoIncludePayer=true 인데 payer가 manualSplits에 포함되어 있지 않습니다.");
        }

        List<TripMember> members = loadAndValidateMembers(tripId, amountMap.keySet());
        members.sort(Comparator.comparingInt(TripMember::getSortOrder));

        int sum = amountMap.values().stream().mapToInt(Integer::intValue).sum();
        if (sum != totalAmount) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "manualSplits 합계(" + sum + ")가 totalAmount(" + totalAmount + ")와 일치하지 않습니다."
            );
        }

        return members.stream()
                .map(member -> ExpensePreviewSplitResponse.builder()
                        .memberId(member.getId())
                        .nickname(member.getNickname())
                        .amount(amountMap.get(member.getId()))
                        .payer(member.getId().equals(payer.getId()))
                        .build())
                .toList();
    }

    private void applyRemainder(
            SplitRemainderPolicy policy,
            Map<Long, Integer> amounts,
            List<TripMember> members,
            Long payerMemberId,
            int remainder
    ) {
        if (remainder <= 0) {
            return;
        }

        if (policy == SplitRemainderPolicy.TO_PAYER) {
            amounts.put(payerMemberId, amounts.get(payerMemberId) + remainder);
            return;
        }

        if (policy == SplitRemainderPolicy.ROUND_ROBIN) {
            for (int i = 0; i < remainder; i++) {
                TripMember target = members.get(i % members.size());
                amounts.put(target.getId(), amounts.get(target.getId()) + 1);
            }
            return;
        }

        // RANDOM
        for (int i = 0; i < remainder; i++) {
            TripMember target = members.get(ThreadLocalRandom.current().nextInt(members.size()));
            amounts.put(target.getId(), amounts.get(target.getId()) + 1);
        }
    }

    private SplitRemainderPolicy resolveSplitPolicy(SettlementSetting setting) {
        if (setting.getSplitRemainderPolicy() != null) {
            return setting.getSplitRemainderPolicy();
        }
        return DEFAULT_SPLIT_POLICY;
    }

    private TripMember getTripMemberOrThrow(Long tripId, Long memberId) {
        if (!tripMemberRepository.existsByIdAndTrip_Id(memberId, tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 trip의 멤버가 아닙니다.");
        }

        return tripMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "TripMember를 찾을 수 없습니다."));
    }

    private List<TripMember> loadAndValidateMembers(Long tripId, Collection<Long> memberIds) {
        List<TripMember> members = tripMemberRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "존재하지 않는 참여자가 포함되어 있습니다.");
        }

        Set<Long> validIds = members.stream()
                .filter(member -> member.getTrip().getId().equals(tripId))
                .map(TripMember::getId)
                .collect(Collectors.toSet());

        if (validIds.size() != memberIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "다른 trip의 멤버가 포함되어 있습니다.");
        }

        return members;
    }
}