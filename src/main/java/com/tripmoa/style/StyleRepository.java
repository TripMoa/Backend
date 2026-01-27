package com.tripmoa.style;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StyleRepository extends JpaRepository<Style, Long> {

    // 이름으로 스타일(ID 등)을 찾기 위해 필요
    Optional<Style> findByName(String name);
}
