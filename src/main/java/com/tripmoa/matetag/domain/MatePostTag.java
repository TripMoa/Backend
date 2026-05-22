package com.tripmoa.matetag.domain;

import com.tripmoa.mate.domain.MatePost;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="mate_post_tag")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatePostTag {

    @EmbeddedId
    private MatePostTagId id;

    @ManyToOne(fetch= FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name="mate_post_id", nullable=false)
    private MatePost post;

    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name="mate_tag_id", nullable=false)
    private MateTag tag;

    private Boolean isAiGenerated = true;
    private LocalDateTime createdAt;

}
