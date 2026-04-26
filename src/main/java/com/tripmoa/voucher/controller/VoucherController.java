package com.tripmoa.voucher.controller;

import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.voucher.dto.VoucherCreateRequest;
import com.tripmoa.voucher.dto.VoucherUpdateRequest;
import com.tripmoa.voucher.dto.VoucherResponse;
import com.tripmoa.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Voucher", description = "바우처 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoucherResponse> createVoucher(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") VoucherCreateRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(voucherService.create(tripId, userId, request, file));
    }

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getVouchers(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(voucherService.getVouchers(tripId, userId));
    }

    @GetMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> getVoucher(
            @PathVariable Long tripId,
            @PathVariable Long voucherId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(voucherService.getVoucher(tripId, voucherId, userId));
    }

    @PutMapping(value = "/{voucherId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long tripId,
            @PathVariable Long voucherId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") VoucherUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(voucherService.update(tripId, voucherId, userId, request, file));
    }

    @DeleteMapping("/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(
            @PathVariable Long tripId,
            @PathVariable Long voucherId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        voucherService.delete(tripId, voucherId, userId);
        return ResponseEntity.noContent().build();
    }
}
