package com.tripmoa.place.controller;

import com.tripmoa.ai.domain.AiClient;
import com.tripmoa.place.dto.PlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PlaceSearchController
 * GET /api/places/search?query=경복궁&display=12
 * → AiClient → Python /schedule/search
 */
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceSearchController {

    private final AiClient aiClient;

    @GetMapping("/search")
    public ResponseEntity<PlaceSearchResponse> search(@RequestParam String query, @RequestParam(defaultValue = "12") int display) {

        PlaceSearchResponse response = aiClient.search(query, display);

        if (response == null) {
            return ResponseEntity.ok(new PlaceSearchResponse());
        }

        return ResponseEntity.ok(response);
    }
}
