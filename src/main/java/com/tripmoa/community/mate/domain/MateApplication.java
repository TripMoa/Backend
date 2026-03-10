package com.tripmoa.community.mate.domain;

import com.tripmoa.community.mate.enums.ApplyStatus;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
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

    public void validatePending() {
        if(this.status != ApplyStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
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

