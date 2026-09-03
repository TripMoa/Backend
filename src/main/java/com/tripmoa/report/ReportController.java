package com.tripmoa.report;

import com.tripmoa.report.dto.MyHiddenTargetsResponse;
import com.tripmoa.report.dto.MyReportHistoryResponse;
import com.tripmoa.report.dto.ReportRequest;
import com.tripmoa.security.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "신고 관련 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> report(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReportRequest request) {

        reportService.report(userDetails.getUser().getId(), request);
        return ResponseEntity.ok().build();
    }

    // 신고 내역 조회
    @GetMapping("/me")
    public ResponseEntity<MyReportHistoryResponse> getMyReportHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        MyReportHistoryResponse response =
                reportService.getMyReportHistory(userDetails.getUser().getId(), page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * 1단계용: 로그인한 사용자가 신고해서 내 화면에서 숨겨야 하는 대상 목록 조회
     */
    @GetMapping("/me/hidden-targets")
    public ResponseEntity<MyHiddenTargetsResponse> getMyHiddenTargets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam ReportLocation location
    ) {
        MyHiddenTargetsResponse response =
                reportService.getMyHiddenTargets(userDetails.getUser().getId(), location);

        return ResponseEntity.ok(response);
    }

}