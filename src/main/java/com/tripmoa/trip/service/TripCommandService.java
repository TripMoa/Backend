package com.tripmoa.trip.service;

import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.PoolBalancePolicy;
import com.tripmoa.expense.enums.SplitRemainderPolicy;
import com.tripmoa.expense.repository.SettlementSettingRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.dto.*;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.enums.TripVisibility;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.repository.TripRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripCommandService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPermissionService tripPermissionService;
    private final UserRepository userRepository;
    private final SettlementSettingRepository settlementSettingRepository;

    // 여행 생성
    public TripDetailResponse createTrip(Long userId, TripCreateRequest request) {
        validateTripDates(request.getTripStartDate(), request.getTripEndDate());

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // null 허용
        List<Long> requestedMemberIds = request.getMemberUserIds() == null
                ? List.of()
                : request.getMemberUserIds();

        // 중복 제거 + 입력 순서 유지
        List<Long> normalizedMemberIds = new ArrayList<>(new LinkedHashSet<>(requestedMemberIds));

        // owner 본인 제외
        normalizedMemberIds.removeIf(memberId -> memberId.equals(owner.getId()));

        // 멤버 존재 여부 재검증
        List<User> memberUsers = normalizedMemberIds.isEmpty()
                ? List.of()
                : userRepository.findAllById(normalizedMemberIds);

        if (memberUsers.size() != normalizedMemberIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "존재하지 않는 초대 멤버가 포함되어 있습니다.");
        }

        // 입력 순서대로 다시 정렬
        Map<Long, User> userMap = memberUsers.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<User> orderedMembers = normalizedMemberIds.stream()
                .map(userMap::get)
                .toList();

        // 초대 멤버가 있으면 PUBLIC, 없으면 PRIVATE
        TripVisibility visibility = orderedMembers.isEmpty()
                ? TripVisibility.PRIVATE
                : TripVisibility.PUBLIC;

        Trip trip = Trip.builder()
                .owner(owner)
                .title(request.getTitle())
                .tripStartDate(request.getTripStartDate())
                .tripEndDate(request.getTripEndDate())
                .visibility(visibility)
                .build();

        Trip savedTrip = tripRepository.save(trip);

        // 정산 설정 기본값 생성
        SettlementSetting setting = SettlementSetting.builder()
                .trip(savedTrip)
                .paymentMode(PaymentMode.HYBRID)
                .splitRemainderPolicy(SplitRemainderPolicy.TO_PAYER)
                .poolBalancePolicy(PoolBalancePolicy.EQUAL)
                .budgetAmount(0)
                .build();

        settlementSettingRepository.save(setting);

        // 오너는 항상 TripMember에 포함
        List<User> allMembers = new ArrayList<>();
        allMembers.add(owner);
        allMembers.addAll(orderedMembers);

        // 닉네임별 전체 등장 횟수 먼저 계산
        Map<String, Long> totalCountMap = allMembers.stream()
                .collect(Collectors.groupingBy(
                        this::resolveNickname,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // 닉네임별 현재까지 붙인 번호
        Map<String, Integer> currentIndexMap = new HashMap<>();

        List<TripMember> tripMembers = new ArrayList<>();

        for (int i = 0; i < allMembers.size(); i++) {
            User memberUser = allMembers.get(i);
            String baseNickname = resolveNickname(memberUser);

            String nickname;
            if (totalCountMap.get(baseNickname) >= 2) {
                int next = currentIndexMap.getOrDefault(baseNickname, 0) + 1;
                currentIndexMap.put(baseNickname, next);
                nickname = baseNickname + "_" + next;
            } else {
                nickname = baseNickname;
            }

            TripMember tripMember = TripMember.builder()
                    .trip(savedTrip)
                    .user(memberUser)
                    .nickname(nickname)
                    .sortOrder(i + 1)
                    .build();

            tripMembers.add(tripMember);
        }

        tripMemberRepository.saveAll(tripMembers);

        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(savedTrip.getId());
        return TripDetailResponse.from(savedTrip, members);
    }

    // 여행 기본 정보 수정
    public TripDetailResponse updateTrip(Long tripId, Long userId, TripUpdateRequest request) {
        tripPermissionService.assertOwner(tripId, userId);
        validateTripDates(request.getTripStartDate(), request.getTripEndDate());

        Trip trip = tripPermissionService.getTripOr404(tripId);
        trip.updateBasicInfo(
                request.getTitle(),
                request.getTripStartDate(),
                request.getTripEndDate()
        );

        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);
        return TripDetailResponse.from(trip, members);
    }

    // 여행 공개 여부 전환
    public TripDetailResponse updateTripVisibility(Long tripId, Long userId, TripVisibility visibility) {
        tripPermissionService.assertOwner(tripId, userId);

        Trip trip = tripPermissionService.getTripOr404(tripId);
        trip.updateVisibility(visibility);

        List<TripMember> members = tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);
        return TripDetailResponse.from(trip, members);
    }

    // 여행 멤버 닉네임 수정 (본인만 가능, 같은 여행 내 중복 불가)
    public TripMemberResponse updateTripMemberNickname(
            Long tripId,
            Long memberId,
            Long userId,
            TripMemberNicknameUpdateRequest request
    ) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        TripMember member = tripMemberRepository.findByIdAndTrip_Id(memberId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_MEMBER_NOT_FOUND));

        // 본인만 수정 가능
        if (member.getUser() == null || !member.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.TRIP_MEMBER_FORBIDDEN, "본인 닉네임만 수정할 수 있습니다.");
        }

        String newNickname = request.getNickname().trim();

        // 같은 값이면 그대로 반환
        if (member.getNickname().equals(newNickname)) {
            return TripMemberResponse.from(member);
        }

        // 같은 여행 내 닉네임 중복 불가
        if (tripMemberRepository.existsByTrip_IdAndNickname(tripId, newNickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME, "이미 사용 중인 여행 멤버 닉네임입니다.");
        }

        member.updateNickname(newNickname);

        return TripMemberResponse.from(member);
    }

    // 여행 삭제 -> 실제 삭제 X, ARCHIVED 처리
    public void deleteTrip(Long tripId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        Trip trip = tripPermissionService.getTripOr404(tripId);
        trip.archive();
    }

    private void validateTripDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "여행 시작일은 종료일보다 늦을 수 없습니다.");
        }
    }

    private String resolveNickname(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return "사용자";
    }
}

