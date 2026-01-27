package com.tripmoa.user.entity;

import com.tripmoa.user.enums.SanctionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_sanctions")
@Getter
@Setter
public class UserSanction {

    // 제채 상태

    @Id
    private Long userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer level = 0;
    private Integer totalReports = 0;

    @Enumerated(EnumType.STRING)
    private SanctionStatus status = SanctionStatus.NORMAL;

}
