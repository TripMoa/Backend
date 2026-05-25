package com.tripmoa.matetag.service;

import com.tripmoa.matetag.dto.MateTagResponse;
import com.tripmoa.matetag.repository.MateTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateTagService {
    private final MateTagRepository tagRepository;

    public List<MateTagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new MateTagResponse(tag.getName(), tag.getCategory()))
                .toList();
    }
}
