package com.tripmoa.voucher.dto;

import com.tripmoa.voucher.entity.Voucher;
import com.tripmoa.voucher.enums.VoucherType;

import java.time.LocalDateTime;

public record VoucherResponse(
        Long voucherId,
        Long tripId,
        VoucherType type,
        String title,
        String description,
        String fileUrl,
        String fileName,
        String fileType,
        Long fileSize,
        Long createdByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static VoucherResponse from(Voucher voucher) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getTrip().getId(),
                voucher.getType(),
                voucher.getTitle(),
                voucher.getDescription(),
                voucher.getFileUrl(),
                voucher.getFileName(),
                voucher.getFileType().name(),
                voucher.getFileSize(),
                voucher.getCreatedByUser() != null ? voucher.getCreatedByUser().getId() : null,
                voucher.getCreatedAt(),
                voucher.getUpdatedAt()
        );
    }
}
