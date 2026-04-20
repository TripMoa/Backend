package com.tripmoa.story.service;

import com.tripmoa.story.domain.Draft;
import com.tripmoa.story.dto.draft.DraftCreateRequest;
import com.tripmoa.story.dto.draft.DraftResponse;
import com.tripmoa.story.dto.draft.DraftUpdateRequest;
import com.tripmoa.story.repository.DraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;



/* 드래프트 서비스
 - 임시 저장된 여행기 조회, 생성, 수정, 삭제 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DraftService {

    private final DraftRepository draftRepository;
    private final UserRepository userRepository;

    // 특정 사용자의 드래프트 목록 조회
    public List<DraftResponse> getDraftsByAuthor(Long authorId) {
        return draftRepository.findByAuthor_IdOrderByUpdatedAtDesc(authorId).stream()
                .map(DraftResponse::from)
                .collect(Collectors.toList());
    }

    // 드래프트 단일 조회
    public DraftResponse getDraft(Long id, Long authorId) {
        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRAFT_NOT_FOUND));

        // 작성자 본인만 조회 가능
        if (!draft.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.DRAFT_ACCESS_FORBIDDEN);
        }

        return DraftResponse.from(draft);
    }

    // 드래프트 생성
    @Transactional
    public DraftResponse createDraft(DraftCreateRequest request, Long authorId) {

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Draft draft = new Draft(
                author,
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                request.getImages(),
                request.getTags(),
                request.getDestination(),
                request.getDuration(),
                request.getDepartureDate(),
                request.getBudget(),
                request.getExpenses()
        );

        Draft saved = draftRepository.save(draft);

        return DraftResponse.from(saved);
    }

    // 드래프트 수정
    @Transactional
    public DraftResponse updateDraft(Long id, DraftUpdateRequest request, Long authorId) {

        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRAFT_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!draft.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.DRAFT_ACCESS_FORBIDDEN);
        }

        draft.update(
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                request.getImages(),
                request.getTags(),
                request.getDestination(),
                request.getDuration(),
                request.getDepartureDate(),
                request.getBudget(),
                request.getExpenses()
        );

        return DraftResponse.from(draft);
    }

    // 드래프트 삭제
    @Transactional
    public void deleteDraft(Long id, Long authorId) {

        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRAFT_NOT_FOUND));

        // 작성자 본인만 삭제 가능
        if (!draft.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.DRAFT_ACCESS_FORBIDDEN);
        }

        draftRepository.deleteById(id);
    }
}