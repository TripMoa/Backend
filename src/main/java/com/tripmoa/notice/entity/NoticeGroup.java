package com.tripmoa.notice.entity;

import com.tripmoa.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notice_group",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notice_group_trip_name",
                        columnNames = {"trip_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_notice_group_trip", columnList = "trip_id"),
                @Index(name = "idx_notice_group_trip_sort", columnList = "trip_id, sort_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeGroup {

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
            foreignKey = @ForeignKey(name = "fk_notice_group_trip")
    )
    private Trip trip;

    /**
     * 그룹명 (예: TRIP NOTICE)
     */
    @Column(name = "name", nullable = false, length = 60)
    private String name;

    /**
     * 기본 그룹 여부
     */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    /**
     * 정렬 순서
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public NoticeGroup(Trip trip, String name, boolean isDefault, Integer sortOrder) {
        this.trip = trip;
        this.name = name;
        this.isDefault = isDefault;
        this.sortOrder = sortOrder;
    }

    public static NoticeGroup createDefault(Trip trip) {
        return new NoticeGroup(trip, "TRIP NOTICE", true, 0);
    }

    public static NoticeGroup create(Trip trip, String name, Integer sortOrder) {
        return new NoticeGroup(trip, name, false, sortOrder);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}