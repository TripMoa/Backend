package com.tripmoa.trip.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.dto.TripDetailResponse;
import com.tripmoa.trip.dto.TripInviteResponse;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.repository.TripRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripInviteService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final TripMemberOrderService tripMemberOrderService;
    private final TripPermissionService tripPermissionService;

    @Transactional(readOnly = true)
    public TripInviteResponse getInviteInfo(String inviteCode, Long userId) {
        Trip trip = getActiveTripByInviteCode(inviteCode);

        List<TripMember> members =
                tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(trip.getId());

        // 이미 멤버인 사람은 링크가 닫혀 있거나 가득 차도 그대로 통과시킨다 (프론트가 워크스페이스로 보낸다)
        boolean alreadyMember = members.stream()
                .anyMatch(m -> m.getUser() != null && m.getUser().getId().equals(userId));
        if (!alreadyMember) {
            assertJoinable(trip, members.size());
        }

        return TripInviteResponse.from(trip, members, alreadyMember);
    }

    // 소유자: 링크 초대 켜기/끄기
    public TripDetailResponse updateInviteEnabled(Long tripId, Long userId, boolean enabled) {
        tripPermissionService.assertOwner(tripId, userId);

        Trip trip = tripPermissionService.getTripOr404(tripId);
        trip.changeInviteEnabled(enabled);

        return TripDetailResponse.from(trip, tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId));
    }

    // 소유자: 초대 코드 다시 만들기 (이전 링크 무효)
    public TripDetailResponse regenerateInviteCode(Long tripId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        Trip trip = tripPermissionService.getTripOr404(tripId);

        String newCode = Trip.newInviteCode();
        while (tripRepository.existsByInviteCode(newCode)) {
            newCode = Trip.newInviteCode();
        }
        trip.regenerateInviteCode(newCode);

        return TripDetailResponse.from(trip, tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId));
    }

    // 여행 행을 먼저 잠근 뒤(이 트랜잭션의 첫 조회) 인원을 센다 — 마지막 한 자리에 여럿이 동시에 들어와도 한 번에 한 명씩만 통과한다.
    // 같은 사람이 동시에 두 번 누른 경우도 두 번째는 잠금을 기다렸다가 "이미 참여 중"으로 거절된다.
    public TripDetailResponse joinTrip(String inviteCode, Long userId) {
        Trip trip = getActiveTripByInviteCode(inviteCode, true);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (tripMemberRepository.existsByTrip_IdAndUser_Id(trip.getId(), userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 참여 중인 여행입니다.");
        }

        int currentMembers = tripMemberRepository.countByTrip_Id(trip.getId());
        assertJoinable(trip, currentMembers);

        return registerMember(trip, user, currentMembers);
    }

    // 소유자: 이미 만들어진 여행에 가입자를 멤버로 추가 (상대 수락 없이 바로 멤버가 된다 — 여행 생성 시 이메일 추가와 같은 방식)
    // 링크 초대가 꺼져 있어도 소유자가 직접 하는 일이라 막지 않고, 인원 상한만 적용한다.
    public TripDetailResponse addMemberByOwner(Long tripId, Long ownerId, Long targetUserId) {
        // 링크 참여와 같은 이유로 여행 행을 먼저 잠근다. 소유자 확인도 잠근 행으로 한다 —
        // 그 전에 일반 조회를 하면 REPEATABLE READ 스냅샷이 잠금 이전 시점으로 잡혀 인원을 낡은 값으로 세게 된다.
        Trip trip = tripRepository.findByIdAndStatusForUpdate(tripId, TripStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));
        if (!trip.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.TRIP_OWNER_REQUIRED);
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "추가할 수 없는 사용자예요.");
        }

        if (tripMemberRepository.existsByTrip_IdAndUser_Id(tripId, targetUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 참여 중인 멤버예요.");
        }

        int currentMembers = tripMemberRepository.countByTrip_Id(tripId);
        assertHasRoom(currentMembers);

        return registerMember(trip, user, currentMembers);
    }

    // 멤버 등록 공통 처리 (링크 참여 / 소유자 추가)
    private TripDetailResponse registerMember(Trip trip, User user, int currentMembers) {
        int nextSortOrder = currentMembers + 1;

        String nickname = createUniqueNickname(trip.getId(), resolveNickname(user));

        TripMember tripMember = TripMember.builder()
                .trip(trip)
                .user(user)
                .nickname(nickname)
                .sortOrder(nextSortOrder)
                .build();

        tripMemberRepository.save(tripMember);

        if (trip.getVisibility() == TripVisibility.PRIVATE) {
            trip.updateVisibility(TripVisibility.PUBLIC);
        }

        tripMemberOrderService.reorder(trip.getId());

        List<TripMember> members =
                tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(trip.getId());

        return TripDetailResponse.from(trip, members);
    }

    // 링크 초대가 닫혀 있거나 인원이 가득 찬 여행에는 새로 참여할 수 없다
    private void assertJoinable(Trip trip, int currentMembers) {
        if (!trip.isInviteEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이 여행은 링크 초대가 닫혀 있어요. 여행 소유자에게 문의해 주세요.");
        }
        assertHasRoom(currentMembers);
    }

    private void assertHasRoom(int currentMembers) {
        if (currentMembers >= Trip.MAX_MEMBERS) {
            throw new BusinessException(ErrorCode.CONFLICT, "참여 인원이 가득 찼어요. (최대 " + Trip.MAX_MEMBERS + "명)");
        }
    }

    private Trip getActiveTripByInviteCode(String inviteCode) {
        return getActiveTripByInviteCode(inviteCode, false);
    }

    // forUpdate: 참여처럼 인원을 바꾸는 작업은 여행 행을 잠그고 조회한다 (미리보기는 잠금 없이 읽기만)
    private Trip getActiveTripByInviteCode(String inviteCode, boolean forUpdate) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "초대코드가 비어 있습니다.");
        }

        String code = inviteCode.trim().toUpperCase();
        return (forUpdate
                ? tripRepository.findByInviteCodeAndStatusForUpdate(code, TripStatus.ACTIVE)
                : tripRepository.findByInviteCodeAndStatus(code, TripStatus.ACTIVE))
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND, "유효하지 않은 초대코드입니다."));
    }

    private String resolveNickname(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return "사용자";
    }

    private String createUniqueNickname(Long tripId, String baseNickname) {
        if (!tripMemberRepository.existsByTrip_IdAndNickname(tripId, baseNickname)) {
            return baseNickname;
        }

        int index = 1;
        while (tripMemberRepository.existsByTrip_IdAndNickname(tripId, baseNickname + "_" + index)) {
            index++;
        }

        return baseNickname + "_" + index;
    }
}