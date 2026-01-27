package com.tripmoa.user.entity;

import com.tripmoa.style.UserStyle;
import com.tripmoa.user.enums.Gender;
import com.tripmoa.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(length = 100)
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

    private String avatarEmoji;
    private String avatarColor;

    private Boolean ageVerified = false;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

