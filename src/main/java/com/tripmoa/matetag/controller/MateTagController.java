package com.tripmoa.matetag.controller;

import com.tripmoa.matetag.dto.MateTagResponse;
import com.tripmoa.matetag.service.MateTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mate")
@RequiredArgsConstructor
public class MateTagController {

    private final MateTagService tagService;

    @GetMapping("/tags")
    public ResponseEntity<List<MateTagResponse>> readTagList() {
        List<MateTagResponse> tags = this.tagService.getAllTags();
        return ResponseEntity.ok().body(tags);
    }
}
