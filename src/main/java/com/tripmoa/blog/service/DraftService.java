package com.tripmoa.blog.service;

import com.tripmoa.blog.domain.Draft;
import com.tripmoa.blog.dto.draft.DraftCreateRequest;
import com.tripmoa.blog.dto.draft.DraftResponse;
import com.tripmoa.blog.dto.draft.DraftUpdateRequest;
import com.tripmoa.blog.repository.DraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/* 드래프트 서비스
 - 임시 저장된 여행기 조회, 생성, 수정, 삭제 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DraftService {

    private final DraftRepository draftRepository;

    // 특정 사용자의 드래프트 목록 조회
    public List<DraftResponse> getDraftsByAuthor(Long authorId) {
        return draftRepository.findByAuthorIdOrderByUpdatedAtDesc(authorId).stream()
                .map(DraftResponse::from)
                .collect(Collectors.toList());
    }

    // 드래프트 단일 조회
    public DraftResponse getDraft(Long id, Long authorId) {
        Draft draft = draftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("임시저장을 찾을 수 없습니다."));

        // 작성자 본인만 조회 가능
        if (!draft.getAuthorId().equals(authorId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        return DraftResponse.from(draft);
    }

    // 드래프트 생성
    @Transactional
    public DraftResponse createDraft(DraftCreateRequest request, Long authorId) {

        Draft draft = new Draft(
                authorId,
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
                .orElseThrow(() -> new RuntimeException("임시저장을 찾을 수 없습니다."));

        // 작성자 본인만 수정 가능
        if (!draft.getAuthorId().equals(authorId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
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
                .orElseThrow(() -> new RuntimeException("임시저장을 찾을 수 없습니다."));

        // 작성자 본인만 삭제 가능
        if (!draft.getAuthorId().equals(authorId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        draftRepository.deleteById(id);
    }
}