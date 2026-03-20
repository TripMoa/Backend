package com.tripmoa.expense.controller;

import com.tripmoa.expense.dto.request.OcrAutofillRequest;
import com.tripmoa.expense.dto.response.OcrAutofillWithPreviewResponse;
import com.tripmoa.expense.dto.response.OcrInitialResponse;
import com.tripmoa.expense.service.OcrMapper;
import com.tripmoa.expense.service.OcrService;
import com.tripmoa.expense.service.TripService;
import com.tripmoa.security.princpal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final OcrMapper ocrMapper;
    private final TripService tripService;

    /**
     * OCR 자동채움 (프론트용)
     * - tripId 기반 권한 체크(여행 멤버 여부 등) 후 OCR 호출
     * - Swagger 파일 파라미터
     */
    @PostMapping(value="/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrInitialResponse> ocrAutofill(
            @PathVariable Long tripId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();

        ocrService.validateFile(file);
        tripService.assertOwnerOrMember(tripId, userId);

        Map<String, Object> raw = ocrService.callOcrApi(file);
        OcrInitialResponse dto = ocrMapper.toAutofillResponse(raw);

        return ResponseEntity.ok(dto);
    }

    /**
     * OCR 자동채움 Test
     * - OCR 결과로 자동채움 데이터를 만들고
     * - 현재 모달 입력값 기준으로 첫 split preview도 함께 반환
     */
    @Operation(summary = "OCR + preview 테스트")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    @PostMapping(value = "/ocr/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrAutofillWithPreviewResponse> ocrAutofillTest(
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