package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.SettlementUpdateAllRequest;
import com.tripmoa.expense.dto.response.*;
import com.tripmoa.expense.entity.*;
import com.tripmoa.expense.enums.DepositLogStatus;
import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.repository.DepositLogRepository;
import com.tripmoa.expense.repository.ExpenseRepository;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Travel Log 화면 조회 전용

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementSummaryService {

    private final TripPermissionService tripPermissionService;
    private final ExpenseRepository expenseRepository;
    private final TripMemberRepository tripMemberRepository;
    private final DepositLogRepository depositLogRepository;

    /**
     * DB 저장된 값으로 모든 요약 정보 조회
     * - 지출, 카테고리별 합계, 멤버별 정산현황, 입금현황, 송금내역
     */
    public SettlementSummaryResponse getSummary(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);
        SettlementSetting setting = tripPermissionService.getSettingOr404(tripId);
        return calculateSummary(tripId, setting);
    }

    /**
     * 입력된 임시 값으로 요약 정보 계산 (미리보기)
     */
    public SettlementSummaryResponse getPreview(Long tripId, Long userId, SettlementUpdateAllRequest request) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        // DB에 저장하지 않고 임시 객체 생성
        SettlementSetting tempSetting = SettlementSetting.builder()
                .paymentMode(request.getPaymentMode())
                .splitRemainderPolicy(request.getSplitRemainderPolicy())
                .poolBalancePolicy(request.getPoolBalancePolicy())
                .budgetAmount(request.getBudgetAmount())
                .build();

        return calculateSummary(tripId, tempSetting);
    }

    /**
     * 공통 계산 로직 (Private 분리)
     * - 기존 getSummary의 로직을 이 메서드로 옮겨서 재사용합니다.
     */
    private SettlementSummaryResponse calculateSummary(Long tripId, SettlementSetting setting) {
        List<Expense> allExpenses = expenseRepository.findAllByTrip_Id(tripId);

        // 공동 지출/개인 지출 합산
        int totalSharedSpent = allExpenses.stream().filter(Expense::isShared).mapToInt(Expense::getTotalAmount).sum();
        int totalPersonalSpent = allExpenses.stream().filter(e -> !e.isShared()).mapToInt(Expense::getTotalAmount).sum();

        // 통계 및 정산 계산 (setting 객체의 모드와 정책을 따름)
        List<SummaryCategorySpendResponse> categorySpend = calculateCategorySpend(allExpenses.stream().filter(Expense::isShared).toList());
        List<Expense> settlementTargetExpenses = filterSettlementExpenses(setting.getPaymentMode(), allExpenses);

        List<SummarySettlementMemberResponse> settlement = calculateSettlement(tripId, setting.getPaymentMode(), settlementTargetExpenses);
        List<SummaryDepositStatusResponse> status = calculateDepositStatus(tripId, setting.getPaymentMode(), setting.getBudgetAmount());
        List<SettlementTransactionResponse> transactions = calculateTransactions(settlement);

        return SettlementSummaryResponse.builder()
                .tripId(tripId)
                .paymentMode(setting.getPaymentMode())
                .budgetAmount(setting.getBudgetAmount())
                .totalSpent(totalSharedSpent)
                .personalTotal(totalPersonalSpent)
                .remainingAmount(setting.getBudgetAmount() - totalSharedSpent)
                .categorySpend(categorySpend)
                .settlement(settlement)
                .status(status)
                .transactions(transactions)
                .build();
    }

    /**
     * 정산(송금 내역) 계산에 포함할 지출 필터링 로직
     */
    private List<Expense> filterSettlementExpenses(PaymentMode paymentMode, List<Expense> allExpenses) {
        return switch (paymentMode) {
            case POOL -> List.of();                                                             // 모임 통장은 송금 계산 안 함
            case INDEPENDENT -> allExpenses;                                                    // 결제 후 정산은 전체(공동+개인) 계산
            case HYBRID -> allExpenses.stream().filter(e -> !e.isShared()).toList();    // 통합 모드는 개인만 계산
        };
    }

    /**
     * 총 지출 합계 계산
     */
    private int calculateTotalSpent(List<Expense> expenses) {
        return expenses.stream()
                .mapToInt(Expense::getTotalAmount)
                .sum();
    }

    /**
     * 카테고리별 지출 금액 집계
     */
    private List<SummaryCategorySpendResponse> calculateCategorySpend(List<Expense> expenses) {
        Map<ExpenseCategory, Integer> categoryMap = new EnumMap<>(ExpenseCategory.class);

        for (Expense expense : expenses) {
            categoryMap.merge(expense.getCategory(), expense.getTotalAmount(), Integer::sum);
        }

        List<SummaryCategorySpendResponse> result = new ArrayList<>();
        for (Map.Entry<ExpenseCategory, Integer> entry : categoryMap.entrySet()) {
            result.add(
                    SummaryCategorySpendResponse.builder()
                            .category(entry.getKey())
                            .amount(entry.getValue())
                            .build()
            );
        }

        return result;
    }

    /**
     * SETTLEMENT : 멤버별 결제 금액(Paid)과 실제 본인 분담액(Shared)의 차액(Balance) 계산
     * - INDEPENDENT, HYBRID 모드에서 주로 사용
     */
    private List<SummarySettlementMemberResponse> calculateSettlement(
            Long tripId,
            PaymentMode paymentMode,
            List<Expense> expenses
    ) {
        if (paymentMode == PaymentMode.POOL) {
            return List.of();
        }

        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);

        Map<Long, Integer> paidMap = new LinkedHashMap<>();
        Map<Long, Integer> sharedMap = new LinkedHashMap<>();

        for (TripMember member : members) {
            paidMap.put(member.getId(), 0);
            sharedMap.put(member.getId(), 0);
        }

        for (Expense expense : expenses) {
            Long payerId = expense.getPayerMember().getId();
            paidMap.put(payerId, paidMap.getOrDefault(payerId, 0) + expense.getTotalAmount());

            for (ExpenseSplit split : expense.getSplits()) {
                Long memberId = split.getMember().getId();
                sharedMap.put(memberId, sharedMap.getOrDefault(memberId, 0) + split.getAmount());
            }
        }

        List<SummarySettlementMemberResponse> result = new ArrayList<>();

        for (TripMember member : members) {
            int paid = paidMap.getOrDefault(member.getId(), 0);
            int shared = sharedMap.getOrDefault(member.getId(), 0);

            result.add(
                    SummarySettlementMemberResponse.builder()
                            .memberId(member.getId())
                            .nickname(member.getNickname())
                            .paidAmount(paid)
                            .sharedAmount(shared)
                            .balance(paid - shared)
                            .build()
            );
        }

        return result;
    }

    /**
     * STATUS : 멤버별 모임 통장 입금 현황 계산
     * - 목표 금액(Target) 대비 승인된 입금액(Deposited) 비교
     */
    private List<SummaryDepositStatusResponse> calculateDepositStatus(
            Long tripId,
            PaymentMode paymentMode,
            int budgetAmount
    ) {
        if (paymentMode == PaymentMode.INDEPENDENT) {
            return List.of();
        }

        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);
        List<DepositLog> depositLogs =
                depositLogRepository.findAllByTrip_IdAndDepositStatus(
                        tripId,
                        DepositLogStatus.CONFIRMED
                );

        if (members.isEmpty()) {
            return List.of();
        }

        // 1차 구현: 목표 입금액은 균등 분배
        int memberCount = members.size();
        int baseTarget = budgetAmount / memberCount;
        int remainder = budgetAmount % memberCount;

        Map<Long, Integer> targetMap = new LinkedHashMap<>();
        for (int i = 0; i < members.size(); i++) {
            TripMember member = members.get(i);
            int target = baseTarget + (i < remainder ? 1 : 0);
            targetMap.put(member.getId(), target);
        }

        Map<Long, Integer> depositedMap = new LinkedHashMap<>();
        for (TripMember member : members) {
            depositedMap.put(member.getId(), 0);
        }

        for (DepositLog log : depositLogs) {
            Long memberId = log.getMember().getId();
            depositedMap.put(memberId, depositedMap.getOrDefault(memberId, 0) + log.getAmount());
        }

        List<SummaryDepositStatusResponse> result = new ArrayList<>();

        for (TripMember member : members) {
            int target = targetMap.getOrDefault(member.getId(), 0);
            int deposited = depositedMap.getOrDefault(member.getId(), 0);
            int remaining = Math.max(target - deposited, 0);
            int overpaid = Math.max(deposited - target, 0);

            String status;
            if (deposited == 0) {
                status = "UNPAID";
            } else if (deposited < target) {
                status = "PARTIAL";
            } else if (deposited == target) {
                status = "PAID";
            } else {
                status = "OVERPAID";
            }

            result.add(
                    SummaryDepositStatusResponse.builder()
                            .memberId(member.getId())
                            .nickname(member.getNickname())
                            .targetAmount(target)
                            .depositedAmount(deposited)
                            .remainingAmount(remaining)
                            .overpaidAmount(overpaid)
                            .status(status)
                            .build()
            );
        }

        return result;
    }

    /**
     * 송금 팝업 : 최종 송금 내역(누가 누구에게 얼마를) 계산
     */
    private List<SettlementTransactionResponse> calculateTransactions(List<SummarySettlementMemberResponse> settlementMembers) {
        if (settlementMembers == null || settlementMembers.isEmpty()) {
            return List.of();
        }

        // 줄 사람(Debtor: balance < 0)과 받을 사람(Creditor: balance > 0) 분리
        List<MemberBalanceTemp> debtors = new ArrayList<>();
        List<MemberBalanceTemp> creditors = new ArrayList<>();

        for (var m : settlementMembers) {
            if (m.balance() < 0) {
                debtors.add(new MemberBalanceTemp(m.memberId(), m.nickname(), Math.abs(m.balance())));
            } else if (m.balance() > 0) {
                creditors.add(new MemberBalanceTemp(m.memberId(), m.nickname(), m.balance()));
            }
        }

        List<SettlementTransactionResponse> transactions = new ArrayList<>();
        int dIdx = 0;
        int cIdx = 0;

        // 서로 매칭하여 리스트 생성
        while (dIdx < debtors.size() && cIdx < creditors.size()) {
            MemberBalanceTemp debtor = debtors.get(dIdx);
            MemberBalanceTemp creditor = creditors.get(cIdx);

            int amount = Math.min(debtor.amount, creditor.amount);

            if (amount > 0) {
                transactions.add(SettlementTransactionResponse.builder()
                        .fromMemberId(debtor.memberId)
                        .fromNickname(debtor.nickname)
                        .toMemberId(creditor.memberId)
                        .toNickname(creditor.nickname)
                        .amount(amount)
                        .build());
            }

            debtor.amount -= amount;
            creditor.amount -= amount;

            if (debtor.amount <= 0) dIdx++;
            if (creditor.amount <= 0) cIdx++;
        }

        return transactions;
    }

    // 계산을 위한 내부 임시 클래스
    private static class MemberBalanceTemp {
        Long memberId;
        String nickname;
        int amount;

        MemberBalanceTemp(Long memberId, String nickname, int amount) {
            this.memberId = memberId;
            this.nickname = nickname;
            this.amount = amount;
        }
    }
}