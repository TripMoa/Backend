package com.tripmoa.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExController {

    private final ExService exService;

    @PostMapping("/api/v1/ocr/connect")
    public ResponseEntity<Map<String, Object>> testOcrConnection(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(exService.callOcrApi(file));
    }

}