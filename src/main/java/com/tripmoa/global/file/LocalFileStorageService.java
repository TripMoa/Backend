package com.tripmoa.global.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 로컬 파일 시스템 기반 파일 저장 구현체
 * - application.properties의 file.upload-dir 경로를 기준으로 파일 저장
 * - 디렉토리별로 파일을 구분하여 저장 (expense, voucher, blog)
 * - 저장 구조: uploads/{directory}/{UUID 파일명}
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    // 업로드 루트 경로 (ex: uploads/)
    private final Path uploadRootPath;

    // 업로드 루트 디렉토리 초기화 (서버 시작 시 uploads 폴더가 없으면 자동 생성)
    public LocalFileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRootPath);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 루트 폴더를 생성할 수 없습니다.", e);
        }
    }

    // 파일 저장 처리
    @Override
    public StoredFileInfo store(MultipartFile file, String directory) {

        // 파일 null 또는 비어있는 경우 예외
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 없습니다.");
        }

        // 디렉토리 값 검증
        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("저장 디렉토리명이 비어 있습니다.");
        }

        // 원본 파일명 정리 (경로 제거 등)
        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );

        // 확장자 추출
        String extension = extractExtension(originalFileName);

        // UUID 기반 파일명 생성 (중복 방지)
        String savedFileName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);

        try {
            // 디렉토리 생성 (ex: uploads/expense)
            Path dirPath = uploadRootPath.resolve(directory).normalize();
            Files.createDirectories(dirPath);

            // 실제 저장 경로
            Path targetPath = dirPath.resolve(savedFileName).normalize();

            // 파일 복사 (덮어쓰기 허용)
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 접근 URL 생성 (WebConfig에서 매핑됨)
            String fileUrl = "/uploads/" + directory + "/" + savedFileName;

            return new StoredFileInfo(
                    fileUrl,
                    savedFileName,
                    originalFileName,
                    file.getSize()
            );
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", e);
        }
    }
    
    // 파일 삭제
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String normalizedUrl = fileUrl.replace("\\", "/");

            // uploads 경로가 아닌 경우 무시
            if (!normalizedUrl.startsWith("/uploads/")) {
                return;
            }

            // 상대 경로 추출
            String relativePath = normalizedUrl.substring("/uploads/".length());

            Path targetPath = uploadRootPath.resolve(relativePath).normalize();

            // 보안: 루트 경로 밖 접근 방지
            if (!targetPath.startsWith(uploadRootPath)) {
                return;
            }

            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            // 삭제 실패해도 서비스 흐름 유지
            System.err.println("파일 삭제 실패: " + fileUrl);
        }
    }

    // 파일 확장자 추출
    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}