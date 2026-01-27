package com.tripmoa.user.entity;

import com.tripmoa.user.enums.Provider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
@Getter
@Setter
public class SocialAccount {

    // 소셜 로그인 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    private String providerUserId;

    private Boolean connected = true;
    private LocalDateTime createdAt;

}