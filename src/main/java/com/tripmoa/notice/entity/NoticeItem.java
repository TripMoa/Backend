package com.tripmoa.notice.entity;

import com.tripmoa.notice.enums.NoticeColor;
import com.tripmoa.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notice_item",
        indexes = {
                @Index(name = "idx_notice_item_group", columnList = "notice_group_id"),
                @Index(name = "idx_notice_item_group_created", columnList = "notice_group_id, created_at"),
                @Index(name = "idx_notice_item_creator", columnList = "created_by_user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 공지 그룹
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notice_group_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notice_item_group")
    )
    private NoticeGroup noticeGroup;

    /**
     * 생성자
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notice_item_created_by")
    )
    private User createdByUser;

    /**
     * 최종 수정자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_by_user_id",
            foreignKey = @ForeignKey(name = "fk_notice_item_updated_by")
    )
    private User updatedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 10)
    private NoticeColor color = NoticeColor.WHITE;

    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public NoticeItem(
            NoticeGroup noticeGroup,
            User createdByUser,
            NoticeColor color,
            String tag,
            String title,
            String content
    ) {
        this.noticeGroup = noticeGroup;
        this.createdByUser = createdByUser;
        this.color = color;
        this.tag = tag;
        this.title = title;
        this.content = content;
    }

    public static NoticeItem create(
            NoticeGroup noticeGroup,
            User createdByUser,
            NoticeColor color,
            String tag,
            String title,
            String content
    ) {
        return new NoticeItem(noticeGroup, createdByUser, color, tag, title, content);
    }

    public void update(
            NoticeColor color,
            String tag,
            String title,
            String content,
            User updatedByUser
    ) {
        this.color = color;
        this.tag = tag;
        this.title = title;
        this.content = content;
        this.updatedByUser = updatedByUser;
    }

    public void moveGroup(NoticeGroup noticeGroup, User updatedByUser) {
        this.noticeGroup = noticeGroup;
        this.updatedByUser = updatedByUser;
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}