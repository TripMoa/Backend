package com.tripmoa.trip.entity;

import com.tripmoa.expense.entity.SettlementSetting;
import com.tripmoa.trip.enums.TripStatus;
import com.tripmoa.trip.enums.TripVisibility;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.tripmoa.story.domain.Story;

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

    // 여행 하나에 함께할 수 있는 최대 인원 (소유자 포함). 여행 생성·링크 참여·소유자 추가가 모두 이 값을 쓴다.
    public static final int MAX_MEMBERS = 10;

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

    // 링크 초대를 닫았는지 여부. 값이 없으면(null) 열려 있는 것으로 본다.
    // not-null 컬럼을 ddl-auto=update로 추가하면 기존 행이 모두 "닫힘"이 될 수 있어서, 닫힘을 true로 표시하는 nullable 컬럼으로 둔다.
    @Column(name = "invite_disabled")
    private Boolean inviteDisabled;

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

    // Story 연관관계 추가
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Story> stories = new ArrayList<>();

    // === 메서드 ===
    @PrePersist
    public void prePersist() {
        // 생성 시점에 고유 코드 자동 생성
        if (this.inviteCode == null) {
            this.inviteCode = newInviteCode();
        }
    }

    public static String newInviteCode() {
        return UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    public boolean isInviteEnabled() {
        return !Boolean.TRUE.equals(inviteDisabled);
    }

    public void changeInviteEnabled(boolean enabled) {
        this.inviteDisabled = !enabled;
    }

    // 이전 초대 링크는 이 순간부터 무효가 된다
    public void regenerateInviteCode(String newCode) {
        this.inviteCode = newCode;
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

    public void changeOwner(User newOwner) {
        this.owner = newOwner;
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
