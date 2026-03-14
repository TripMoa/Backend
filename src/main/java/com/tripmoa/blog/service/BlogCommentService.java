package com.tripmoa.blog.service;

import com.tripmoa.blog.domain.BlogComment;
import com.tripmoa.blog.dto.comment.CommentCreateRequest;
import com.tripmoa.blog.dto.comment.CommentResponse;
import com.tripmoa.blog.repository.BlogCommentRepository;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tripmoa.global.util.BadWordFilter;

import java.util.List;
import java.util.stream.Collectors;

/* 댓글 서비스
 - 댓글 조회, 생성, 수정, 삭제 기능 처리 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BadWordFilter badWordFilter;

    // 특정 게시글의 댓글 목록 조회
    public List<CommentResponse> getComments(Long blogId) {
        return commentRepository.findByBlogId(blogId).stream()
                .map(comment -> {

                    // 작성자 정보 조회
                    User user = userRepository.findById(comment.getAuthorId())
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                    return CommentResponse.from(comment, user);
                })
                .collect(Collectors.toList());
    }

    // 댓글 생성
    @Transactional
    public CommentResponse createComment(Long blogId, CommentCreateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (badWordFilter.containsBadWord(request.getContent())) {
            throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다.");
        }

        BlogComment comment = new BlogComment(blogId, authorId, request.getContent());
        BlogComment saved = commentRepository.save(comment);

        // 작성자 정보 조회
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return CommentResponse.from(saved, user);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long authorId) {

        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 본인만 삭제 가능
        if (!comment.getAuthorId().equals(authorId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        commentRepository.deleteById(commentId);
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentCreateRequest request, Long authorId) {

        // 욕설 필터 검사
        if (badWordFilter.containsBadWord(request.getContent())) {
            throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다.");
        }

        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 본인만 수정 가능
        if (!comment.getAuthorId().equals(authorId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        comment.updateContent(request.getContent());

        // 작성자 정보 조회
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return CommentResponse.from(comment, user);
    }

}