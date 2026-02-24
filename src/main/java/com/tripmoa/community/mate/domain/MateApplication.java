package com.tripmoa.community.mate.domain;

import com.tripmoa.community.mate.enums.ApplyStatus;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MateApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private MatePost matePost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", referencedColumnName = "id")
    private User applicant;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private ApplyStatus status;

    private void validatePending() {
        if(this.status != ApplyStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 신청서만 처리할 수 있습니다.");
        }
    }

    public void approve() {
        validatePending();
        this.status = ApplyStatus.APPROVED;
        this.matePost.incCurrentParticipant();
    }

    public void reject() {
        validatePending();
        this.status = ApplyStatus.REJECTED;
    }

    public boolean isAuthor(User author) {
        return this.matePost.getUser().getId().equals(author.getId());
    }

}

