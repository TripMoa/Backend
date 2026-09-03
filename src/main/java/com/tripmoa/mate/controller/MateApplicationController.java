package com.tripmoa.mate.controller;

import com.tripmoa.mate.dto.ApplicationRequest;
import com.tripmoa.mate.dto.ApplicationResponse;
import com.tripmoa.mate.service.MateApplicationService;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate")
public class MateApplicationController {

    private final MateApplicationService applyService;

    // 내가 받은 메이트 신청서 조회
    @GetMapping("/applications/received")
    public ResponseEntity<List<ApplicationResponse>> getReceivedApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<ApplicationResponse> applications = this.applyService.getReceivedApplication(userId);
        return ResponseEntity.ok().body(applications);
    }

    // 내가 보낸 메이트 신청서 조회
    @GetMapping("/applications/sent")
    public ResponseEntity<List<ApplicationResponse>> getSentApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<ApplicationResponse> applications = this.applyService.getSentApplication(userId);
        return ResponseEntity.ok().body(applications);
    }


    // 특정 메이트 신청
    @PostMapping("/{id}/apply/applicant")
    public ResponseEntity<ApplicationResponse> applicateMatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ApplicationRequest request
    ) {
        User applicant = userDetails.getUser();
        ApplicationResponse apply = this.applyService.createApply(id, request, applicant);
        return ResponseEntity.ok().body(apply);
    }

    // 메이트 신청 승인
    @PutMapping("/applications/{applyId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User author = userDetails.getUser();
        ApplicationResponse approveApply = this.applyService.approveApply(applyId, author);
        return ResponseEntity.ok().body(approveApply);
    }

    // 메이트 신청 거절
    @PutMapping("/applications/{applyId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User author = userDetails.getUser();
        ApplicationResponse rejectApply = this.applyService.rejectApply(applyId, author);
        return ResponseEntity.ok().body(rejectApply);
    }

    @DeleteMapping("/applications/{applyId}/sent")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User applicant = userDetails.getUser();
        this.applyService.deleteSentApplication(applyId, applicant);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/applications/{applyId}/received")
    public ResponseEntity<Void> deleteReceivedApplication(
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User author = userDetails.getUser();
        this.applyService.deleteReceivedApplication(applyId, author);
        return ResponseEntity.noContent().build();
    }
}
