package com.tripmoa.community.chat.repository;

import com.tripmoa.community.chat.domain.ChatRoom;
import com.tripmoa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 참여한 채팅방 조회
    @Query("SELECT DISTINCT r FROM ChatRoom r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.applicant " +
            "JOIN FETCH r.matePost " +
            "LEFT JOIN FETCH r.messages m " +
            "WHERE (r.author = :member AND r.authorLeft = false) " +
            "OR (r.applicant = :member AND r.applicantLeft = false) " +
            "ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findAllByMemberNotLeft(@Param("member") User member);

    // 게시글 + 신청자 조합 검색
    Optional<ChatRoom> findByMatePostIdAndApplicantId(Long matePostId, Long applicantId);

    // 게시글 아이디로 채팅방 찾기
    List<ChatRoom> findAllByMatePostId(Long matePostId);
}