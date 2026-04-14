package com.tripmoa.trip.service;

import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.expense.enums.PaymentMode;
import com.tripmoa.expense.enums.PoolBalancePolicy;
import com.tripmoa.expense.enums.SplitRemainderPolicy;
import com.tripmoa.expense.repository.SettlementSettingRepository;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.notice.entity.NoticeGroup;
import com.tripmoa.notice.entity.NoticeItem;
import com.tripmoa.notice.entity.NoticeTag;
import com.tripmoa.notice.enums.NoticeColor;
import com.tripmoa.notice.repository.NoticeGroupRepository;
import com.tripmoa.notice.repository.NoticeItemRepository;
import com.tripmoa.notice.repository.NoticeTagRepository;
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
    private final NoticeGroupRepository noticeGroupRepository;
    private final NoticeItemRepository noticeItemRepository;
    private final NoticeTagRepository noticeTagRepository;

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

        // 기본 공지 그룹 생성
        NoticeGroup defaultNoticeGroup = NoticeGroup.createDefault(savedTrip);
        noticeGroupRepository.save(defaultNoticeGroup);

        // 기본 공지 4개 생성
        List<NoticeItem> defaultNoticeItems = List.of(
                NoticeItem.create(
                        defaultNoticeGroup,
                        owner,
                        NoticeColor.YELLOW,
                        "준비물",
                        "여행 준비물 체크리스트",
                        """
                        여행 전 필요한 준비물을 미리 확인해주세요.
        
                        - 신분증 / 여권
                        - 충전기 / 보조배터리
                        - 세면도구 / 개인 상비약
                        - 옷 / 우산 / 계절용품
                        - 현금 / 카드 / 교통수단 예매 확인
        
                        출발 전날 한 번 더 체크해주세요.
                        """
                ),
                NoticeItem.create(
                        defaultNoticeGroup,
                        owner,
                        NoticeColor.BLUE,
                        "연락처",
                        "비상 연락처",
                        """
                        긴급 상황에 대비해 주요 연락처를 정리해주세요.
        
                        - 숙소 연락처
                        - 병원 / 약국
                        - 경찰서 / 긴급 신고 번호
                        - 차량 렌트 / 보험사
                        - 여행 멤버 비상 연락처
        
                        필요한 번호는 여행 전 미리 공유해주세요.
                        """
                ),
                NoticeItem.create(
                        defaultNoticeGroup,
                        owner,
                        NoticeColor.GREEN,
                        "예약",
                        "일정 및 예약 유의사항",
                        """
                        예약 및 일정 변경 사항은 모든 멤버가 확인할 수 있게 공유해주세요.
        
                        - 숙소 체크인 / 체크아웃 시간 확인
                        - 기차 / 버스 / 항공권 시간 재확인
                        - 입장권 / 예약 바우처 보관
                        - 지각 방지를 위해 출발 시간 10분 전 도착 권장
        
                        일정 변경 시 공지사항에 바로 남겨주세요.
                        """
                ),
                NoticeItem.create(
                        defaultNoticeGroup,
                        owner,
                        NoticeColor.WHITE,
                        "안전",
                        "안전 주의사항",
                        """
                        안전한 여행을 위해 아래 내용을 확인해주세요.
        
                        - 늦은 시간 단독 이동 주의
                        - 귀중품 분실 주의
                        - 낯선 장소에서는 위치 공유 권장
                        - 무리한 일정 진행 금지
                        - 비상 상황 시 바로 연락 후 함께 대응
        
                        모두가 안전하게 여행할 수 있도록 서로 확인해주세요.
                        """
                )
        );

        noticeItemRepository.saveAll(defaultNoticeItems);

        // 기본 태그 생성
        List<String> defaultTags = List.of("준비물", "연락처", "예약", "안전");

        List<NoticeTag> noticeTags = defaultTags.stream()
                .distinct()
                .map(tag -> NoticeTag.create(savedTrip, tag))
                .toList();

        noticeTagRepository.saveAll(noticeTags);

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

