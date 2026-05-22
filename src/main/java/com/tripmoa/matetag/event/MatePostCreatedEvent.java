package com.tripmoa.matetag.event;

import java.time.LocalDate;

public record MatePostCreatedEvent
        (Long postId,
         String content,
         String destination
        ) {
}
