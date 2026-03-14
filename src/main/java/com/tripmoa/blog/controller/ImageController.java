package com.tripmoa.blog.controller;

import com.tripmoa.blog.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // 이미지 업로드 (POST /api/images)
    @PostMapping
    public List<String> uploadImages(
            @RequestPart("images") List<MultipartFile> images
    ) {
        return imageService.upload(images);
    }
}