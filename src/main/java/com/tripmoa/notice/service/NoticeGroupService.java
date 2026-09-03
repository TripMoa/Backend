package com.tripmoa.notice.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.notice.dto.request.NoticeGroupCreateRequest;
import com.tripmoa.notice.dto.request.NoticeGroupRenameRequest;
import com.tripmoa.notice.dto.response.NoticeGroupResponse;
import com.tripmoa.notice.entity.NoticeGroup;
import com.tripmoa.notice.repository.NoticeGroupRepository;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeGroupService {

    private final NoticeGroupRepository noticeGroupRepository;
    private final TripPermissionService tripPermissionService;

    /**
     * 공지 그룹 전체 조회
     * - 여행 멤버 이상 접근 가능
     */
    public List<NoticeGroupResponse> getNoticeGroups(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        return noticeGroupRepository.findByTrip_IdOrderBySortOrderAscCreatedAtAsc(tripId)
                .stream()
                .map(NoticeGroupResponse::from)
                .toList();
    }

    /**
     * 공지 그룹 단건 조회
     * - 여행 멤버 이상 접근 가능
     */
    public NoticeGroupResponse getNoticeGroup(Long tripId, Long groupId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        NoticeGroup noticeGroup = getNoticeGroupOrThrow(tripId, groupId);
        return NoticeGroupResponse.from(noticeGroup);
    }

    /**
     * 공지 그룹 생성
     * - 소유주만 가능
     */
    @Transactional
    public NoticeGroupResponse createNoticeGroup(Long tripId, Long userId, NoticeGroupCreateRequest request) {
        tripPermissionService.assertOwner(tripId, userId);

        String name = request.name().trim();
        validateDuplicateGroupName(tripId, name);

        Trip trip = tripPermissionService.getTripOr404(tripId);

        NoticeGroup noticeGroup = NoticeGroup.create(
                trip,
                name,
                request.sortOrder() != null ? request.sortOrder() : 0
        );

        noticeGroupRepository.save(noticeGroup);
        return NoticeGroupResponse.from(noticeGroup);
    }

    /**
     * 공지 그룹 이름 수정
     * - 소유주만 가능
     * - 기본 그룹(TRIP NOTICE)은 이름 수정 불가
     */
    @Transactional
    public NoticeGroupResponse renameNoticeGroup(Long tripId, Long groupId, Long userId, NoticeGroupRenameRequest request) {
        tripPermissionService.assertOwner(tripId, userId);

        NoticeGroup noticeGroup = getNoticeGroupOrThrow(tripId, groupId);

        if (noticeGroup.isDefault()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "기본 공지 그룹은 이름을 수정할 수 없습니다.");
        }

        String name = request.name().trim();
        validateDuplicateGroupNameForUpdate(tripId, groupId, name);

        noticeGroup.rename(name);
        return NoticeGroupResponse.from(noticeGroup);
    }

    /**
     * 공지 그룹 삭제
     * - 소유주만 가능
     * - 기본 그룹(TRIP NOTICE)은 삭제 불가
     */
    @Transactional
    public void deleteNoticeGroup(Long tripId, Long groupId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        NoticeGroup noticeGroup = getNoticeGroupOrThrow(tripId, groupId);

        if (noticeGroup.isDefault()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "기본 공지 그룹은 삭제할 수 없습니다.");
        }

        noticeGroupRepository.delete(noticeGroup);
    }

    private NoticeGroup getNoticeGroupOrThrow(Long tripId, Long groupId) {
        return noticeGroupRepository.findByIdAndTrip_Id(groupId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_GROUP_NOT_FOUND));
    }

    private void validateDuplicateGroupName(Long tripId, String name) {
        if (noticeGroupRepository.existsByTrip_IdAndName(tripId, name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NOTICE_GROUP_NAME);
        }
    }

    private void validateDuplicateGroupNameForUpdate(Long tripId, Long groupId, String name) {
        if (noticeGroupRepository.existsByTrip_IdAndNameAndIdNot(tripId, name, groupId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NOTICE_GROUP_NAME);
        }
    }
}