package com.tripmoa.blog.controller;

import com.tripmoa.blog.dto.tag.TagListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* TravelStyle 태그 관련 API 컨트롤러 (프론트엔드에서 사용 가능한 태그 목록을 제공) */

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        allowCredentials = "true"
)
public class TagController {

    /* 전체 태그 목록 조회
      GET /api/tags (@return 사용 가능한 모든 태그 목록) */
    @GetMapping
    public ResponseEntity<List<TagListResponse>> getAllTags() {
        // 하드코딩된 태그 목록 (추후 DB 테이블로 관리 가능)
        List<String> tagNames = Arrays.asList(
                "힐링여행", "액티비티", "맛집탐방", "문화탐방",
                "쇼핑", "자연", "사진", "야경",
                "로컬체험", "카페투어", "축제", "역사탐방",
                "야외활동", "미식투어", "럭셔리", "배낭여행"
        );

        // TagListResponse 형태로 변환 (id는 1부터 순차적으로)
        List<TagListResponse> tags = tagNames.stream()
                .map(name -> {
                    int index = tagNames.indexOf(name);
                    return new TagListResponse((long) (index + 1), name);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(tags);
    }
}