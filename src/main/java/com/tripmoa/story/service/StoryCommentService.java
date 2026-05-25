package com.tripmoa.story.service;

import com.tripmoa.story.domain.StoryComment;
import com.tripmoa.story.dto.comment.CommentCreateRequest;
import com.tripmoa.story.dto.comment.CommentResponse;
import com.tripmoa.story.repository.StoryCommentRepository;
import com.tripmoa.story.repository.StoryRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tripmoa.global.util.BadWordFilter;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import com.tripmoa.story.domain.Story;
import com.tripmoa.report.ReportService;
import com.tripmoa.report.ReportLocation;
import com.tripmoa.report.ContentStatus;

/* 댓글 서비스
 - 댓글 조회, 생성, 수정, 삭제 기능 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryCommentService {

    private final StoryCommentRepository storyCommentRepository;
    private final UserRepository userRepository;
    private final BadWordFilter badWordFilter;
    private final StoryRepository storyRepository;
    private final ReportService reportService;



    // 댓글 생성
    @Transactional
    public CommentResponse createComment(Long storyId, CommentCreateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (badWordFilter.containsBadWord(request.getContent())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        StoryComment comment = new StoryComment(story, user, request.getContent());
        StoryComment saved = storyCommentRepository.save(comment);

        return CommentResponse.from(saved);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long authorId) {

        StoryComment comment = storyCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인만 삭제 가능
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        storyCommentRepository.deleteById(commentId);
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentCreateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (badWordFilter.containsBadWord(request.getContent())) {
            throw new BusinessException(ErrorCode.BAD_WORD_DETECTED);
        }

        StoryComment comment = storyCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }

        comment.updateContent(request.getContent());

        return CommentResponse.from(comment);

    }

    // 특정 게시글의 댓글 목록 조회
    public List<CommentResponse> getComments(Long storyId) {
        return storyCommentRepository.findByStory_Id(storyId).stream()
                .map(comment -> {
                    CommentResponse response = CommentResponse.from(comment);
                    // 신고 횟수 체크 - 3회 이상이면 내용 숨김
                    ContentStatus status = reportService.resolveContentStatus(
                            ReportLocation.COMMENT, comment.getId()
                    );
                    if (status == ContentStatus.REPORTED) {
                        return CommentResponse.reported(comment);  // 숨김 처리된 응답
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

}