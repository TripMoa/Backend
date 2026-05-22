package com.tripmoa.matetag.repository;

import com.tripmoa.matetag.domain.MatePostTag;
import com.tripmoa.matetag.domain.MatePostTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatePostTagRepository extends JpaRepository<MatePostTag, MatePostTagId> {
}
