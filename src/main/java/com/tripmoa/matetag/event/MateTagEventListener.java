package com.tripmoa.matetag.event;

import com.tripmoa.matetag.client.FastApiTagClient;
import com.tripmoa.matetag.domain.MatePostTag;
import com.tripmoa.matetag.domain.MatePostTagId;
import com.tripmoa.matetag.domain.MateTag;
import com.tripmoa.matetag.dto.FastApiTagRequest;
import com.tripmoa.matetag.dto.FastApiTagResponse;
import com.tripmoa.matetag.repository.MatePostTagRepository;
import com.tripmoa.matetag.repository.MateTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
@Slf4j
public class MateTagEventListener {

    private final FastApiTagClient fastApiTagClient;
    private final MateTagRepository tagRepository;
    private final MatePostTagRepository postTagRepository;

    @Async
    @TransactionalEventListener(phase=AFTER_COMMIT)
    public void handleTagExtraction(MatePostCreatedEvent event) {
        try {
            FastApiTagResponse response = fastApiTagClient.extractTags(
                    new FastApiTagRequest(
                            event.postId(),
                            event.content(),
                            event.destination(),
                            event.budget(),
                            event.memberCount(),
                            event.startDate(),
                            event.endDate()
                    )
            );

            List<String> tagNames = new ArrayList<>();
            tagNames.addAll(response.styleTags());
            tagNames.addAll(response.vibeTags());

            List<MateTag> tags = tagRepository.findByNameIn(tagNames);

            List<MatePostTag> postTags = tags.stream()
                    .map(tag -> MatePostTag.builder()
                            .id(new MatePostTagId(event.postId(), tag.getId()))
                            .tag(tag)
                            .build())
                    .toList();

            postTagRepository.saveAll(postTags);
        } catch (Exception e) {
            log.error("태그 추출 실패 - postId: {}", event.postId(), e);
        }
    }

}
