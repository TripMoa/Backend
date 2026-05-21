package com.tripmoa.expense.controller;

import com.tripmoa.expense.dto.request.OcrAutofillRequest;
import com.tripmoa.expense.dto.response.OcrAutofillWithPreviewResponse;
import com.tripmoa.expense.service.OcrService;
import com.tripmoa.security.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "OCR", description = "영수증 OCR 자동채움 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/expenses")
public class OcrController {

    private final OcrService ocrService;

    /**
     * OCR 자동채움
     * - OCR 결과로 자동채움 + 데이터 후처리 + LLM 연결
     */
    @Operation(summary = "OCR + preview 테스트")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrAutofillWithPreviewResponse> ocrAutofill(
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") @Valid OcrAutofillRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();

        return ResponseEntity.ok(
                ocrService.autofillWithPreview(tripId, userId, file, request)
        );
    }

}