package com.tripmoa.notice.entity;

import com.tripmoa.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notice_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notice_tag_trip_name",
                        columnNames = {"trip_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_notice_tag_trip", columnList = "trip_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 여행
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trip_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notice_tag_trip")
    )
    private Trip trip;

    /**
     * 태그명
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public NoticeTag(Trip trip, String name) {
        this.trip = trip;
        this.name = name;
    }

    public static NoticeTag create(Trip trip, String name) {
        return new NoticeTag(trip, name);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}