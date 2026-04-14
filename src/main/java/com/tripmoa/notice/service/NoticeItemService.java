package com.tripmoa.notice.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.notice.dto.request.NoticeItemCreateRequest;
import com.tripmoa.notice.dto.request.NoticeItemUpdateRequest;
import com.tripmoa.notice.dto.response.NoticeItemResponse;
import com.tripmoa.notice.dto.response.NoticeTagResponse;
import com.tripmoa.notice.entity.NoticeGroup;
import com.tripmoa.notice.entity.NoticeItem;
import com.tripmoa.notice.entity.NoticeTag;
import com.tripmoa.notice.enums.NoticeColor;
import com.tripmoa.notice.repository.NoticeGroupRepository;
import com.tripmoa.notice.repository.NoticeItemRepository;
import com.tripmoa.notice.repository.NoticeTagRepository;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.service.TripPermissionService;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeItemService {

    private final NoticeItemRepository noticeItemRepository;
    private final NoticeGroupRepository noticeGroupRepository;
    private final NoticeTagRepository noticeTagRepository;
    private final TripPermissionService tripPermissionService;
    private final UserRepository userRepository;

    /**
     * 공지 메모 전체 조회
     * - 여행 멤버 이상 접근 가능
     */
    public List<NoticeItemResponse> getNoticeItems(Long tripId, Long groupId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        NoticeGroup noticeGroup = getNoticeGroupOrThrow(tripId, groupId);

        return noticeItemRepository
                .findByNoticeGroup_Trip_IdAndNoticeGroup_IdOrderByIsPinnedDescCreatedAtDesc(
                        tripId,
                        noticeGroup.getId()
                )
                .stream()
                .map(NoticeItemResponse::from)
                .toList();
    }

    /**
     * 공지 메모 단건 조회
     * - 여행 멤버 이상 접근 가능
     */
    public NoticeItemResponse getNoticeItem(Long tripId, Long noticeItemId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        NoticeItem noticeItem = getNoticeItemOrThrow(tripId, noticeItemId);
        return NoticeItemResponse.from(noticeItem);
    }

    /**
     * 공지 메모 생성
     * - 여행 멤버 이상 가능
     */
    @Transactional
    public NoticeItemResponse createNoticeItem(Long tripId, Long userId, NoticeItemCreateRequest request) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        NoticeGroup noticeGroup = getNoticeGroupOrThrow(tripId, request.noticeGroupId());
        User user = getUserOrThrow(userId);

        NoticeColor color = request.color() != null ? request.color() : NoticeColor.WHITE;
        String tag = normalizeTag(request.tag());
        String title = request.title().trim();
        String content = request.content().trim();

        NoticeItem noticeItem = NoticeItem.create(
                noticeGroup,
                user,
                color,
                tag,
                title,
                content
        );

        noticeItemRepository.save(noticeItem);
        saveTagIfNeeded(tripId, tag);

        return NoticeItemResponse.from(noticeItem);
    }

    /**
     * 공지 메모 수정
     * - 여행 멤버 이상 가능
     */
    @Transactional
    public NoticeItemResponse updateNoticeItem(Long tripId, Long noticeItemId, Long userId, NoticeItemUpdateRequest request) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        NoticeItem noticeItem = getNoticeItemOrThrow(tripId, noticeItemId);
        NoticeGroup targetGroup = getNoticeGroupOrThrow(tripId, request.noticeGroupId());
        User user = getUserOrThrow(userId);

        if (!noticeItem.getNoticeGroup().getId().equals(targetGroup.getId())) {
            noticeItem.moveGroup(targetGroup, user);
        }

        String tag = normalizeTag(request.tag());

        noticeItem.update(
                request.color(),
                tag,
                request.title().trim(),
                request.content().trim(),
                user
        );

        saveTagIfNeeded(tripId, tag);

        return NoticeItemResponse.from(noticeItem);
    }

    /**
     * 공지 메모 삭제
     * - 여행 소유주만 가능
     */
    @Transactional
    public void deleteNoticeItem(Long tripId, Long noticeItemId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        NoticeItem noticeItem = getNoticeItemOrThrow(tripId, noticeItemId);
        noticeItemRepository.delete(noticeItem);
    }

    /**
     * 공지 메모 핀 고정
     * - 여행 멤버 이상 가능
     */
    @Transactional
    public NoticeItemResponse pinNoticeItem(Long tripId, Long noticeItemId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        NoticeItem noticeItem = getNoticeItemOrThrow(tripId, noticeItemId);
        noticeItem.pin();

        return NoticeItemResponse.from(noticeItem);
    }

    /**
     * 공지 메모 핀 해제
     * - 여행 소유주만 가능
     */
    @Transactional
    public NoticeItemResponse unpinNoticeItem(Long tripId, Long noticeItemId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        NoticeItem noticeItem = getNoticeItemOrThrow(tripId, noticeItemId);
        noticeItem.unpin();

        return NoticeItemResponse.from(noticeItem);
    }

    /**
     * 최근 사용 태그 조회
     * - 여행 멤버 이상 접근 가능
     */
    public List<NoticeTagResponse> getRecentNoticeTags(Long tripId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        return noticeTagRepository.findTop10ByTrip_IdOrderByCreatedAtDesc(tripId)
                .stream()
                .map(NoticeTagResponse::from)
                .toList();
    }

    /**
     * 태그 삭제
     * - 여행 소유주만 가능
     */
    @Transactional
    public void deleteNoticeTag(Long tripId, Long tagId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        NoticeTag noticeTag = noticeTagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_TAG_NOT_FOUND));

        if (!noticeTag.getTrip().getId().equals(tripId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 여행의 공지 태그가 아닙니다.");
        }

        noticeTagRepository.delete(noticeTag);
    }

    private NoticeItem getNoticeItemOrThrow(Long tripId, Long noticeItemId) {
        return noticeItemRepository.findByIdAndNoticeGroup_Trip_Id(noticeItemId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_ITEM_NOT_FOUND));
    }

    private NoticeGroup getNoticeGroupOrThrow(Long tripId, Long groupId) {
        return noticeGroupRepository.findByIdAndTrip_Id(groupId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_GROUP_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeTag(String tag) {
        if (tag == null) {
            return null;
        }

        String normalized = tag.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private void saveTagIfNeeded(Long tripId, String normalizedTag) {
        if (normalizedTag == null) {
            return;
        }

        if (noticeTagRepository.existsByTrip_IdAndName(tripId, normalizedTag)) {
            return;
        }

        Trip trip = tripPermissionService.getTripOr404(tripId);
        NoticeTag noticeTag = NoticeTag.create(trip, normalizedTag);
        noticeTagRepository.save(noticeTag);
    }
}