package com.tripmoa.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_visibility")
@Getter
@Setter
public class UserVisibility {
    
    // 정보 공개 설정

    @Id
    private Long userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean showName=true;
    private Boolean showAge=true;
    private Boolean showGender=true;

}
