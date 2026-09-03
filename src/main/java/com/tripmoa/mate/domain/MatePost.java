package com.tripmoa.mate.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tripmoa.mate.enums.AgeGroup;
import com.tripmoa.mate.enums.GenderPreference;
import com.tripmoa.mate.enums.Transport;
import com.tripmoa.matetag.domain.MatePostTag;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatePost {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(length = 500)
    private String content;

    // 도착지
    @Column(length = 100, nullable = false)
    private String destination;

    // 출발 일정 - 도착 일정
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    // 여행 참여인원
    @Column(nullable = false)
    private Integer currentParticipant;
    @Column(nullable = false)
    private Integer maxParticipant;

    // 예산
    @Column(nullable = false)
    private Integer budget;

    // 이동수단
    @Enumerated(EnumType.STRING)
    private Transport transport;

    // 성별 선호
    @Enumerated(EnumType.STRING)
    private GenderPreference genderPreference;

    // 나이 선호
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    @BatchSize(size = 20)
    @Builder.Default
    private List<MatePostTag> tags = new ArrayList<>();

    // 조회수, 좋아요
    @Column
    @Builder.Default
    private Long likesCount = 0L;
    @Column
    @Builder.Default
    private Long viewsCount = 0L;

    @Column(nullable = false)
    private LocalDateTime  createdAt;



    public void incViewsCount() {this.viewsCount++;}
    public void incLikesCount() {this.likesCount++;}
    public void decLikesCount() {
        if(this.likesCount>0) {
            this.likesCount--;
        }
    }

    public void incCurrentParticipant() {this.currentParticipant++;}
    public void decCurrentParticipant() {this.currentParticipant--;}

    public boolean isExpired() {
        return this.endDate.isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }
}
