package com.tripmoa.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_notification_settings")
@Getter
@Setter
public class UserNotificationSettings {
    
    // 알림 설정

    @Id
    private Long userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean tripAlert=true;
    private Boolean arketingAlert=false;
    private Boolean emailAlert=true;

}
