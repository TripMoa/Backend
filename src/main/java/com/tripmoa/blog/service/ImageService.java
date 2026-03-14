package com.tripmoa.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* 이미지 업로드 서비스
 - 업로드된 이미지를 서버의 uploads 폴더에 저장 */

@Service
public class ImageService {

    // 프로젝트 루트의 uploads 폴더 경로
    private final Path root = Paths.get("uploads").toAbsolutePath().normalize();

    public ImageService() {
        // 서비스 시작 시 uploads 폴더 생성
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
                System.out.println("Created upload directory: " + root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    // 이미지 업로드 처리
    public List<String> upload(List<MultipartFile> images) {

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : images) {

            // 원본 파일명 확인
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "image.jpg";
            }

            // UUID를 이용한 고유 파일명 생성
            String filename = UUID.randomUUID() + "_" + originalFilename;

            try {

                // 파일 저장
                Path filePath = root.resolve(filename);
                Files.copy(file.getInputStream(), filePath);

                // 프론트에서 사용할 URL 반환
                urls.add("/uploads/" + filename);

                System.out.println("Saved image: " + filePath);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save image: " + filename, e);
            }
        }

        return urls;
    }
}