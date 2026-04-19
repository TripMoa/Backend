package com.tripmoa.mate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ApplicationRequest {

    @NotBlank(message = "신청 내용은 필수입니다")
    @Size(max = 500, message = "500자를 초과할 수 없습니다")
    private String content;

}
