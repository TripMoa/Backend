package com.tripmoa.style;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 여행 스타일 자동 생성
@Component
@RequiredArgsConstructor
public class TravelStyleDataInitializer implements CommandLineRunner {

    private final StyleRepository styleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (styleRepository.count() > 0) {
            return;
        }

        List<String> styleNames = List.of(
                "맛집탐방", "액티비티", "힐링", "문화탐방",
                "쇼핑", "자연", "사진", "야경",
                "로컬체험", "카페투어", "축제", "역사탐방",
                "야외활동", "미식투어", "럭셔리", "배낭여행"
        );

        List<Style> styles = styleNames.stream()
                .map(name -> {
                    Style style = new Style();
                    style.setName(name);
                    return style;
                })
                .toList();

        styleRepository.saveAll(styles);
    }
}