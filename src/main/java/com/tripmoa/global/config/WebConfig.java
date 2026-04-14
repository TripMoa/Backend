package com.tripmoa.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/* Web MVC 설정 (이미지)
 * - 정적 리소스 경로 설정
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /* 정적 리소스 핸들러 설정
     - /uploads/** 경로의 요청을 uploads/ 폴더로 매핑 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 경로를 uploads/ 디렉토리로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600); // 1시간 캐시
    }

}
