package com.tripmoa.chat.repository;

import com.tripmoa.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방의 메시지 조회 (최신순, 페이징)
    @Query("SELECT m FROM ChatMessage m " +
            "JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId " +
            "ORDER BY m.createdAt ASC")
    List<ChatMessage> findAllByChatRoomId(@Param("roomId") Long roomId);

     // 특정 채팅방의 최근 메시지 N건 조회 (역순 페이징)
    @Query("SELECT m FROM ChatMessage m " +
            "JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessages(@Param("roomId") Long roomId, Pageable pageable);
}