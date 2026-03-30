package com.tripmoa.trip.entity;

import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "trip",
        indexes = {
                @Index(name = "idx_trip_owner", columnList = "owner_user_id"),
                @Index(name = "idx_trip_status", columnList = "status"),
                @Index(name = "idx_trip_visibility", columnList = "visibility"),
                @Index(name = "idx_trip_dates", columnList = "trip_start_date, trip_end_date"),
                @Index(name = "uk_trip_invite_code", columnList = "invite_code", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // owner_user_id (FK -> users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_owner_user"))
    private User owner;

    @Column(length = 80, nullable = false)
    private String title;

    @Column(name = "trip_start_date", nullable = false)
    private LocalDate tripStartDate;

    @Column(name = "trip_end_date", nullable = false)
    private LocalDate tripEndDate;

    @Column(name = "invite_code", nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.ACTIVE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripVisibility visibility = TripVisibility.PRIVATE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === 연관관계 ===

    @OneToOne(mappedBy = "trip", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private SettlementSetting settlementSetting;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripMember> members = new ArrayList<>();

    // === 메서드 ===
    @PrePersist
    public void prePersist() {
        // 생성 시점에 고유 코드 자동 생성
        if (this.inviteCode == null) {
            this.inviteCode = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        }
    }

    // 초대 링크를 반환하는 편의 메서드
    public String getInviteLink(String baseUrl) {
        return String.format("%s/trips/join?code=%s", baseUrl, this.inviteCode);
    }

    public void attachSettlementSetting(SettlementSetting setting) {
        this.settlementSetting = setting;
        setting.setTrip(this);
    }

    public void addMember(TripMember member) {
        members.add(member);
        member.setTrip(this);
    }

    public void removeMember(TripMember member) {
        members.remove(member);
        member.setTrip(null);
    }

    public void updateBasicInfo(String title, LocalDate tripStartDate, LocalDate tripEndDate) {
        this.title = title;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
    }

    public void updateVisibility(TripVisibility visibility) {
        this.visibility = visibility;
    }

    public void archive() {
        this.status = TripStatus.ARCHIVED;
    }
}
