package com.tripmoa.trip.service;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.entity.TripMember;
import com.tripmoa.trip.repository.TripMemberRepository;
import com.tripmoa.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripMemberOrderService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;

    public void reorder(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow();

        Long ownerId = trip.getOwner().getId();

        List<TripMember> members =
                tripMemberRepository.findAllByTrip_IdOrderBySortOrderAsc(tripId);

        List<TripMember> sortedMembers = members.stream()
                .sorted(
                        Comparator
                                .comparing((TripMember member) -> !isOwner(member, ownerId))
                                .thenComparing(this::isInactive)
                                .thenComparing(TripMember::getSortOrder)
                                .thenComparing(TripMember::getId)
                )
                .toList();

        for (int i = 0; i < sortedMembers.size(); i++) {
            sortedMembers.get(i).updateSortOrder(i + 1);
        }
    }

    private boolean isOwner(TripMember member, Long ownerId) {
        return member.getUser() != null
                && member.getUser().getId().equals(ownerId);
    }

    private boolean isInactive(TripMember member) {
        return member.getUser() == null
                || "알수 없음".equals(member.getNickname());
    }
}