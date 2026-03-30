package com.tripmoa.trip.entity;

import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trip_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_trip_member_nickname", columnNames = {"trip_id", "nickname"})
        },
        indexes = {
                @Index(name = "idx_trip_member_trip", columnList = "trip_id"),
                @Index(name = "idx_trip_member_user", columnList = "user_id"),
                @Index(name = "idx_trip_member_trip_created", columnList = "trip_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // trip_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_member_trip"))
    private Trip trip;

    // user_id (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_trip_member_user"))
    private User user;

    @Column(length = 30, nullable = false)
    private String nickname;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Trip에서 메서드로 세팅할 수 있게 setter 열어둠
    void setTrip(Trip trip) {
        this.trip = trip;
    }

    // === 메서드 ===
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}