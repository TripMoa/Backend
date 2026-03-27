package com.tripmoa.expense.service;

import com.tripmoa.expense.dto.request.ExpensePreviewRequest;
import com.tripmoa.expense.dto.request.OcrAutofillRequest;
import com.tripmoa.expense.dto.response.ExpensePreviewResponse;
import com.tripmoa.expense.dto.response.OcrAutofillResponse;
import com.tripmoa.expense.dto.response.OcrAutofillWithPreviewResponse;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${naver.ocr.invoke-url}")
    private String invokeUrl;

    @Value("${naver.ocr.secret-key}")
    private String secretKey;

    @Value("${naver.ocr.api-gateway-key-id:}")
    private String apiGatewayKeyId;

    @Value("${naver.ocr.api-gateway-key:}")
    private String apiGatewayKey;

    @Value("${naver.ocr.domain-id:}")
    private String domainId;

    @Value("${naver.ocr.signature:}")
    private String signature;

    @Value("${naver.ocr.path:}")
    private String path;

    private final RestTemplate restTemplate;
    private final TripPermissionService tripPermissionService;
    private final OcrMapper ocrMapper;
    private final SettlementPreviewService settlementPreviewService;

    // 프록시 컨트롤러에서 먼저 호출할 파일 검증
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.OCR_BAD_REQUEST, "파일이 비어있습니다.");
        }

        String filename = file.getOriginalFilename();
        String ext = getExt(filename);

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        if (!("jpg".equals(ext) || "png".equals(ext) || "pdf".equals(ext))) {
            throw new BusinessException(ErrorCode.OCR_BAD_REQUEST, "지원하지 않는 파일 형식입니다. (jpg/png/pdf)");
        }
    }

    /**
     * OCR 자동채움 + 첫 split preview 반환
     */
    public OcrAutofillWithPreviewResponse autofillWithPreview(
            Long tripId,
            Long userId,
            MultipartFile file,
            OcrAutofillRequest request
    ) {
        validateFile(file);
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Map<String, Object> raw = callOcrApi(file);
        OcrAutofillResponse autofill = ocrMapper.toAutofillResult(raw);

        Integer totalAmount = autofill.totalAmount();
        if (totalAmount == null || totalAmount <= 0) {
            throw new BusinessException(ErrorCode.OCR_PROCESS_ERROR, "OCR 결과에서 유효한 총 금액을 추출하지 못했습니다.");
        }

        ExpensePreviewRequest previewRequest = new ExpensePreviewRequest(
                request.payerMemberId(),
                totalAmount,
                request.isShared(),
                request.autoIncludePayer(),
                request.splitMode(),
                request.joinedMemberIds() == null ? List.of() : request.joinedMemberIds(),
                null
        );

        ExpensePreviewResponse preview =
                settlementPreviewService.preview(tripId, userId, previewRequest);

        return new OcrAutofillWithPreviewResponse(autofill, preview);
    }

    public Map<String, Object> callOcrApi(MultipartFile file) {
        try {
            // 인코딩 이슈 방지
            DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
            uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
            restTemplate.setUriTemplateHandler(uriFactory);

            // body 숨김 방지
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

            // 최종 URL 생성
            String url = buildFinalUrl();

            // 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // OCR Secret
            headers.set("X-OCR-SECRET", secretKey);

            if (!isBlank(apiGatewayKeyId) && !isBlank(apiGatewayKey)) {
                headers.set("X-NCP-APIGW-API-KEY-ID", apiGatewayKeyId);
                headers.set("X-NCP-APIGW-API-KEY", apiGatewayKey);
            }

            // multipart body: file + message(JSON 문자열)
            MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();

            // file
            multipartBody.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() == null ? "upload.jpg" : file.getOriginalFilename();
                }
            });

            // message (템플릿 OCR 공통 형식)
            String ext = getExt(file.getOriginalFilename());
            String messageJson = buildMessageJson(ext);
            multipartBody.add("message", messageJson);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartBody, headers);

            // 호출
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {});

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new BusinessException(ErrorCode.OCR_PROCESS_ERROR,
                        "네이버 OCR 응답 body가 비어있습니다.");
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(ErrorCode.OCR_PROCESS_ERROR,
                        "네이버 OCR 비정상 응답: " + response.getStatusCode());
            }

            return body;

        } catch (HttpStatusCodeException e) {
            throw new BusinessException(ErrorCode.OCR_PROCESS_ERROR, "네이버 OCR 에러: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OCR_PROCESS_ERROR, "OCR 호출 중 오류 발생: " + e.getMessage());
        }
    }

    private String buildFinalUrl() {
        if (isBlank(domainId) || isBlank(signature) || isBlank(path)) {
            return invokeUrl;
        }

        String base = invokeUrl;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        String p = path;
        if (p.startsWith("/")) p = p.substring(1);

        return base + "/" + domainId + "/" + signature + "/" + p;
    }

    private String buildMessageJson(String format) {
        return """
        {
          "version": "V2",
          "requestId": "%s",
          "timestamp": %d,
          "images": [
            {
              "format": "%s",
              "name": "receipt_test"
            }
          ]
        }
        """.formatted(UUID.randomUUID(), System.currentTimeMillis(), format);
    }

    private String getExt(String filename) {
        if (filename == null) return "jpg";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return "jpg";
        String ext = filename.substring(idx + 1).toLowerCase();
        if ("jpeg".equals(ext)) return "jpg";
        return ext;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}