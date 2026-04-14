package com.tripmoa.global.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장/삭제 공통 인터페이스
 * - 로컬 저장, S3 등 저장소 구현을 분리하기 위한 추상화 계층
 * - 실제 저장 방식은 구현체에서 처리
 * - 확장: LocalFileStorageService (현재) -> S3FileStorageService (추후)
 */
public interface FileStorageService {

    // 파일을 지정한 디렉토리에 저장
    StoredFileInfo store(MultipartFile file, String directory);

    // 파일 삭제
    void deleteFile(String fileUrl);
}