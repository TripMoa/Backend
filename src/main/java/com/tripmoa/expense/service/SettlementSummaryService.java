package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.response.SettlementSummaryResponse;
import com.tripmoa.expense.dto.response.SummaryCategorySpendResponse;
import com.tripmoa.expense.dto.response.SummaryDepositStatusResponse;
import com.tripmoa.expense.dto.response.SummarySettlementMemberResponse;
import com.tripmoa.expense.entity.*;
import com.tripmoa.expense.enums.DepositLogStatus;
import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.repository.DepositLogRepository;
import com.tripmoa.expense.repository.ExpenseRepository;
import com.tripmoa.expense.repository.TripMemberRepository;
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

    private final TripService tripService;
    private final ExpenseRepository expenseRepository;
    private final TripMemberRepository tripMemberRepository;
    private final DepositLogRepository depositLogRepository;

    public SettlementSummaryResponse getSummary(Long tripId, Long userId) {
        tripService.assertOwnerOrMember(tripId, userId);

        SettlementSetting setting = tripService.getSettingOr404(tripId);

        List<Expense> allExpenses = expenseRepository.findAllByTrip_Id(tripId);
        List<Expense> targetExpenses = filterExpensesByPaymentMode(setting.getPaymentMode(), allExpenses);

        int totalSpent = calculateTotalSpent(targetExpenses);
        int remainingAmount = setting.getBudgetAmount() - totalSpent;

        List<SummaryCategorySpendResponse> categorySpend = calculateCategorySpend(targetExpenses);
        List<SummarySettlementMemberResponse> settlement = calculateSettlement(
                tripId,
                setting.getPaymentMode(),
                targetExpenses
        );
        List<SummaryDepositStatusResponse> status = calculateDepositStatus(
                tripId,
                setting.getPaymentMode(),
                setting.getBudgetAmount()
        );

        return SettlementSummaryResponse.builder()
                .tripId(tripId)
                .paymentMode(setting.getPaymentMode())
                .budgetAmount(setting.getBudgetAmount())
                .totalSpent(totalSpent)
                .remainingAmount(remainingAmount)
                .categorySpend(categorySpend)
                .settlement(settlement)
                .status(status)
                .build();
    }

    private List<Expense> filterExpensesByPaymentMode(PaymentMode paymentMode, List<Expense> expenses) {
        if (paymentMode == PaymentMode.POOL) {
            return expenses.stream()
                    .filter(Expense::isShared)
                    .toList();
        }

        // INDEPENDENT, HYBRID 는 전체 지출 사용
        return expenses;
    }

    private int calculateTotalSpent(List<Expense> expenses) {
        return expenses.stream()
                .mapToInt(Expense::getTotalAmount)
                .sum();
    }

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

    private List<SummaryDepositStatusResponse> calculateDepositStatus(
            Long tripId,
            PaymentMode paymentMode,
            int budgetAmount
    ) {
        if (paymentMode == PaymentMode.INDEPENDENT) {
            return List.of();
        }

        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);
        List<DepositLog> depositLogs = depositLogRepository.findAllByTrip_IdAndDepositStatus(tripId, DepositLogStatus.CONFIRMED);

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
}