package com.tripmoa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * RedisConfig
 *
 * - Redis 연결 설정 클래스
 * - Lettuce 기반 RedisConnectionFactory 생성
 * - RedisTemplate Bean 등록 (Key: String, Value: JSON 직렬화)
 * - Redis Repository 사용 가능하도록 설정
 */

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(RedisSerializer.string()); // key
        template.setValueSerializer(RedisSerializer.json()); // value
        return template;
    }
}

