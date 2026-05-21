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

    @Transactional(readOnly = true)
    public TripInviteResponse getInviteInfo(String inviteCode) {
        Trip trip = getActiveTripByInviteCode(inviteCode);

        List<TripMember> members =
                tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(trip.getId());

        return TripInviteResponse.from(trip, members);
    }

    public TripDetailResponse joinTrip(String inviteCode, Long userId) {
        Trip trip = getActiveTripByInviteCode(inviteCode);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (tripMemberRepository.existsByTrip_IdAndUser_Id(trip.getId(), userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 참여 중인 여행입니다.");
        }

        int nextSortOrder = tripMemberRepository.countByTrip_Id(trip.getId()) + 1;

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

    private Trip getActiveTripByInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "초대코드가 비어 있습니다.");
        }

        return tripRepository.findByInviteCodeAndStatus(inviteCode.trim().toUpperCase(), TripStatus.ACTIVE)
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