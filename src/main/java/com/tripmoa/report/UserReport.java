package com.tripmoa.report;

import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_reports")
@Getter
@Setter
public class UserReport {
    
    // 신고 이력

    @Id
    @GeneratedValue(strategy = GenerationType .IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ReportLocation location;

    private String reason;
    private LocalDate reportedAt;
}
