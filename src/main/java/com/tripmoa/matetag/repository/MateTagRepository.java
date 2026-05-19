package com.tripmoa.matetag.repository;

import com.tripmoa.matetag.domain.MateTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MateTagRepository extends JpaRepository<MateTag, Long> {
    List<MateTag> findByNameIn(List<String> tagNames);
}
