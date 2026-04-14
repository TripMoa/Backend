package com.tripmoa.global.file;

/**
 * 파일 저장 결과 DTO
 * - 파일 저장 후 필요한 정보를 묶어서 반환
 * - 각 도메인에서 필요한 값만 사용
 * - expense → receipt_url, receipt_file_name
 * - voucher → file_url, file_name, file_size
 * - blog → image_url
 */
public record StoredFileInfo(

        // 접근 가능한 파일 URL (/uploads/...)
        String fileUrl,

        // 서버에 저장된 파일명 (UUID 기반)
        String fileName,

        // 원본 파일명 (사용자 업로드 이름)
        String originalFileName,

        // 파일 크기 (bytes)
        Long fileSize
) {
}