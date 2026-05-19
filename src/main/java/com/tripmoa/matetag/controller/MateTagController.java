package com.tripmoa.matetag.controller;

import com.tripmoa.matetag.dto.MateTagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mate")
public class MateTagController {

    @GetMapping("/tags")
    public ResponseEntity<List<MateTagResponse>> readTagList() {
        List<MateTagResponse> tags = null;
        return ResponseEntity.ok().body(tags);
    }
}
