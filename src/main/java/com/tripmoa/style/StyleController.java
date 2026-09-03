package com.tripmoa.style;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class StyleController {

    private final StyleRepository styleRepository;

    @GetMapping("/styles")
    public List<StyleDto> getStyles() {
        return styleRepository.findAll()
                .stream()
                .map(style -> new StyleDto(style.getId(), style.getName()))
                .toList();
    }
}