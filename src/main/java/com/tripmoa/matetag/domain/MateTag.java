package com.tripmoa.matetag.domain;

import com.tripmoa.matetag.enums.MateTagCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MateTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private MateTagCategory category;

}
