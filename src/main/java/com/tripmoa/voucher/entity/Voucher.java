package com.tripmoa.voucher.entity;

import com.tripmoa.trip.entity.Trip;
import com.tripmoa.user.entity.User;
import com.tripmoa.voucher.enums.VoucherFileType;
import com.tripmoa.voucher.enums.VoucherType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "voucher",
        indexes = {
                @Index(name = "idx_voucher_trip", columnList = "trip_id"),
                @Index(name = "idx_voucher_trip_type", columnList = "trip_id, type"),
                @Index(name = "idx_voucher_created_by", columnList = "created_by_user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 바우처 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private VoucherType type;

    // 바우처 제목
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 바우처 설명
    @Column(name = "description", length = 255)
    private String description;

    // 업로드 파일 접근 경로
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    // 원본 파일명
    @Column(name = "file_name", length = 255)
    private String fileName;

    // 파일 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private VoucherFileType fileType;

    // 파일 크기(byte)
    @Column(name = "file_size")
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 바우처가 속한 여행
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_trip"))
    private Trip trip;

    // 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id",
            foreignKey = @ForeignKey(name = "fk_voucher_created_by_user"))
    private User createdByUser;

    // === 메서드 ===
    @Builder
    public Voucher(Trip trip,
                   VoucherType type,
                   String title,
                   String description,
                   String fileUrl,
                   String fileName,
                   VoucherFileType fileType,
                   Long fileSize,
                   User createdByUser) {
        this.trip = trip;
        this.type = type;
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdByUser = createdByUser;
    }

    public void update(VoucherType type,
                       String title,
                       String description,
                       String fileUrl,
                       String fileName,
                       VoucherFileType fileType,
                       Long fileSize) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public void changeFile(String fileUrl, String fileName, VoucherFileType fileType, Long fileSize) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
}
