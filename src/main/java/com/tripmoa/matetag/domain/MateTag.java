package com.tripmoa.matetag.domain;

import com.tripmoa.matetag.enums.MateTagCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name="mate_tags")
@Getter
@NoArgsConstructor
@BatchSize(size=20)
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
