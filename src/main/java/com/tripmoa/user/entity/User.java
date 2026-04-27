package com.tripmoa.user.entity;

import com.tripmoa.style.UserStyle;
import com.tripmoa.user.enums.AgeVerificationStatus;
import com.tripmoa.user.enums.Gender;
import com.tripmoa.user.enums.ProfileType;
import com.tripmoa.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

// User 정보

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    private String name;
    private String email;

    @Column(length = 255)
    private String notificationEmail;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    @Column(nullable = false)
    private boolean nameLocked = false;

    @Column(nullable = false)
    private boolean genderLocked = false;

    @Column(nullable = false)
    private boolean birthLocked = false;

    @Column(length = 4)
    private String mbti;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserStyle> travelStyles = new ArrayList<>();

    @Column(columnDefinition = "MEDIUMTEXT")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private ProfileType profileType = ProfileType.AVATAR;

    private String avatarEmoji;
    private String avatarColor;

    @Column(nullable = false)
    private Boolean ageVerified = false;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // === 메서드 ===
    public void verifyAdult() {
        this.ageVerified = true;
    }

    public void resetAgeVerification() {
        this.ageVerified = false;
    }

    public boolean hasBirthDate() {
        return this.birthDate != null;
    }

    public boolean isAdultByBirthDate() {
        if (this.birthDate == null) {
            return false;
        }
        return Period.between(this.birthDate, LocalDate.now()).getYears() >= 19;
    }

    public AgeVerificationStatus getAgeVerificationStatus() {
        if (Boolean.TRUE.equals(this.ageVerified)) {
            return AgeVerificationStatus.VERIFIED;
        }

        if (this.birthDate == null) {
            return AgeVerificationStatus.UNVERIFIED;
        }

        return isAdultByBirthDate()
                ? AgeVerificationStatus.UNVERIFIED
                : AgeVerificationStatus.UNDERAGE;
    }
}

