package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.ExpenseCreateRequest;
import com.tripmoa.expense.dto.request.ExpenseSplitCreateRequest;
import com.tripmoa.expense.dto.request.ExpensePreviewManualSplitRequest;
import com.tripmoa.expense.dto.request.ExpensePreviewRequest;
import com.tripmoa.expense.dto.response.ExpenseDetailResponse;
import com.tripmoa.expense.dto.response.ExpensePreviewResponse;
import com.tripmoa.expense.dto.response.ExpenseResponse;
import com.tripmoa.expense.entity.Expense;
import com.tripmoa.expense.entity.ExpenseSplit;
import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.SplitMode;
import com.tripmoa.expense.repository.ExpenseRepository;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.repository.TripRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.service.TripPermissionService;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final TripPermissionService tripPermissionService;
    private final SettlementPreviewService settlementPreviewService;

    @Transactional(readOnly = true)
    public List<ExpenseDetailResponse> getExpenses(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        List<Expense> expenses = expenseRepository.findAllWithDetailsByTripId(tripId);

        return expenses.stream()
                .map(ExpenseDetailResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseDetailResponse getExpense(Long tripId, Long expenseId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Expense expense = expenseRepository.findDetailByTripIdAndExpenseId(tripId, expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        return ExpenseDetailResponse.from(expense);
    }

    public ExpenseResponse create(Long tripId, Long userId, ExpenseCreateRequest req) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));

        SettlementSetting setting = tripPermissionService.getSettingOr404(tripId);

        validateSharedByPaymentMode(setting, req.isShared());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        TripMember payer = getValidPayer(tripId, req.payerMemberId());
        LocalDateTime paidAt = parsePaidAt(req.paidAt());

        List<ExpenseSplitCreateRequest> finalSplits = resolveFinalSplits(tripId, userId, req, payer);

        Expense expense = Expense.builder()
                .trip(trip)
                .createdBy(user)
                .payerMember(payer)
                .autoIncludePayer(req.autoIncludePayer() == null ? true : req.autoIncludePayer())
                .paidAt(paidAt)
                .storeName(req.storeName())
                .itemMemo(req.itemMemo())
                .totalAmount(req.totalAmount())
                .shared(req.isShared())
                .category(req.category())
                .payMethod(req.payMethod())
                .receiptUrl(req.receiptUrl())
                .receiptFileName(req.receiptFileName())
                .splitMode(req.splitMode())
                .build();

        for (ExpenseSplit split : buildSplits(expense, finalSplits)) {
            expense.addSplit(split);
        }

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.from(saved);
    }

    public ExpenseResponse update(Long tripId, Long expenseId, Long userId, ExpenseCreateRequest req) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 trip의 영수증이 아닙니다.");
        }

        SettlementSetting setting = tripPermissionService.getSettingOr404(tripId);

        validateSharedByPaymentMode(setting, req.isShared());

        TripMember payer = getValidPayer(tripId, req.payerMemberId());
        LocalDateTime paidAt = parsePaidAt(req.paidAt());

        List<ExpenseSplitCreateRequest> finalSplits = resolveFinalSplits(tripId, userId, req, payer);

        expense.updateExpense(
                payer,
                req.autoIncludePayer() == null ? true : req.autoIncludePayer(),
                paidAt,
                req.storeName(),
                req.itemMemo(),
                req.totalAmount(),
                req.category(),
                req.payMethod(),
                req.isShared(),
                req.receiptUrl(),
                req.receiptFileName(),
                req.splitMode()
        );

        expense.clearSplits();
        expenseRepository.flush();

        for (ExpenseSplit split : buildSplits(expense, finalSplits)) {
            expense.addSplit(split);
        }

        return ExpenseResponse.from(expense);
    }

    public void delete(Long tripId, Long expenseId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 trip의 영수증이 아닙니다.");
        }

        expenseRepository.delete(expense);
    }

    private void validateSharedByPaymentMode(SettlementSetting setting, boolean isShared) {
        if (setting.getPaymentMode() == PaymentMode.POOL && !isShared) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "모임 통장 모드에서는 개인 지출을 등록할 수 없습니다."
            );
        }
    }

    private List<ExpenseSplitCreateRequest> resolveFinalSplits(
            Long tripId,
            Long userId,
            ExpenseCreateRequest req,
            TripMember payer
    ) {
        if (req.splitMode() == SplitMode.EQUAL) {
            ExpensePreviewResponse preview = settlementPreviewService.preview(
                    tripId,
                    userId,
                    toPreviewRequest(req, payer)
            );

            if (!preview.canSave()) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        preview.message() == null ? "분할 미리보기 계산에 실패했습니다." : preview.message()
                );
            }

            List<ExpenseSplitCreateRequest> result = new ArrayList<>();
            preview.splits().forEach(split ->
                    result.add(new ExpenseSplitCreateRequest(split.memberId(), split.amount()))
            );
            return result;
        }

        validateManualSplits(tripId, payer, req.totalAmount(), req.autoIncludePayer(), req.splits());
        return req.splits();
    }

    private ExpensePreviewRequest toPreviewRequest(ExpenseCreateRequest req, TripMember payer) {
        List<Long> joinedMemberIds = req.splits() == null
                ? List.of()
                : req.splits().stream()
                .map(ExpenseSplitCreateRequest::memberId)
                .toList();

        List<ExpensePreviewManualSplitRequest> manualSplits = req.splits() == null
                ? List.of()
                : req.splits().stream()
                .map(s -> new ExpensePreviewManualSplitRequest(s.memberId(), s.amount()))
                .toList();

        return new ExpensePreviewRequest(
                payer.getId(),
                req.totalAmount(),
                req.isShared(),
                req.autoIncludePayer(),
                req.splitMode(),
                joinedMemberIds,
                manualSplits
        );
    }

    private TripMember getValidPayer(Long tripId, Long payerMemberId) {
        if (!tripMemberRepository.existsByIdAndTrip_Id(payerMemberId, tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "payerMemberId가 해당 trip의 멤버가 아닙니다.");
        }

        return tripMemberRepository.findById(payerMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
    }

    private LocalDateTime parsePaidAt(String paidAt) {
        try {
            return LocalDateTime.parse(
                    paidAt,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "결제 일시 형식이 올바르지 않습니다. (yyyy-MM-dd HH:mm:ss)"
            );
        }
    }

    private void validateManualSplits(
            Long tripId,
            TripMember payer,
            int totalAmount,
            Boolean autoIncludePayer,
            List<ExpenseSplitCreateRequest> splits
    ) {
        boolean includePayer = (autoIncludePayer == null) ? true : autoIncludePayer;

        if (splits == null || splits.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "직접 입력 분할에는 splits가 필요합니다.");
        }

        List<Long> memberIds = new ArrayList<>();
        int sum = 0;
        boolean payerIncluded = false;

        for (ExpenseSplitCreateRequest s : splits) {
            if (s == null || s.memberId() == null || s.amount() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "splits 항목이 올바르지 않습니다.");
            }

            if (s.amount() < 0) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "split 금액은 0 이상이어야 합니다.");
            }

            if (memberIds.contains(s.memberId())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "splits에 중복 memberId가 포함되어 있습니다.");
            }

            memberIds.add(s.memberId());

            if (!tripMemberRepository.existsByIdAndTrip_Id(s.memberId(), tripId)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "splits의 memberId가 해당 trip의 멤버가 아닙니다.");
            }

            if (payer.getId().equals(s.memberId())) {
                payerIncluded = true;
            }

            sum += s.amount();
        }

        if (includePayer && !payerIncluded) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "autoIncludePayer=true 인데 payer가 splits에 포함되어 있지 않습니다.");
        }

        if (sum != totalAmount) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "splits 합계(" + sum + ")가 totalAmount(" + totalAmount + ")와 일치하지 않습니다."
            );
        }
    }

    private List<ExpenseSplit> buildSplits(
            Expense expense,
            List<ExpenseSplitCreateRequest> splits
    ) {
        List<ExpenseSplit> result = new ArrayList<>();

        for (ExpenseSplitCreateRequest s : splits) {
            if (s.amount() == null || s.amount() <= 0) {
                continue;
            }

            TripMember member = tripMemberRepository.findById(s.memberId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "TripMember 조회 실패"));

            result.add(
                    ExpenseSplit.builder()
                            .expense(expense)
                            .member(member)
                            .amount(s.amount())
                            .build()
            );
        }

        return result;
    }
}