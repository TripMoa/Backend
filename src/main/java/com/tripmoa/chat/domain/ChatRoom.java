package com.tripmoa.chat.domain;

import com.tripmoa.mate.domain.MatePost;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"mate_post_id", "applicant_id"}  // 같은 게시글+신청자 조합은 1개만
        ))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결된 메이트 모집 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_post_id", nullable = false)
    private MatePost matePost;

    // 게시글 작성자 (방장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 신청자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    // 채팅 메시지 목록
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private boolean authorLeft = false;

    @Column(nullable = false)
    private boolean applicantLeft = false;

    // === 비즈니스 메서드 ===

    public void updateLastMessageAt(LocalDateTime time) {
        this.lastMessageAt = time;
    }

    // 채팅방 회원 확인
    public boolean isMember(User user) {
        return author.getId().equals(user.getId())
                || applicant.getId().equals(user.getId());
    }

    // 상대방 조회
    public User getOtherMember(User me) {
        return author.getId().equals(me.getId()) ? applicant : author;
    }

    //
    public void markLeft(User user) {
        if(user.getId().equals(author.getId())) {
            this.authorLeft = true;
        } else if(user.getId().equals(applicant.getId())) {
            this.applicantLeft = true;
        }
    }

    public boolean hasLeft(User user) {
        if(user.getId().equals(author.getId())) return authorLeft;
        if(user.getId().equals(applicant.getId())) return applicantLeft;
        return false;
    }

    public boolean bothLeft() {
        return authorLeft && applicantLeft;
    }
}