package com.tripmoa.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckEmailResponse {
    private boolean exists;
    private Long userId;
    private String email;
    private String name;
}
