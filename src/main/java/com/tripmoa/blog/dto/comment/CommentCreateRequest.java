package com.tripmoa.blog.dto.comment;

/* 댓글 생성 요청 DTO
 - 댓글 작성 시 프론트에서 전달되는 데이터 */
public class CommentCreateRequest {

    // 댓글 내용
    private String content;

    public CommentCreateRequest() {}

    public CommentCreateRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}