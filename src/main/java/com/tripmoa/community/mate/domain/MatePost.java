package com.tripmoa.community.mate.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tripmoa.community.mate.enums.AgeGroup;
import com.tripmoa.community.mate.enums.GenderPreference;
import com.tripmoa.community.mate.enums.Transport;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

//    @Enumerated(EnumType.STRING)
//    private Set<Tag> tags;

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

}
