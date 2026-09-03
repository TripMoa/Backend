package com.tripmoa.style;

import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_travel_styles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "style_id"}))
@Getter
@Setter
public class UserStyle {

    // User-스타일 매핑

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "style_id")
    private Style style;

}
