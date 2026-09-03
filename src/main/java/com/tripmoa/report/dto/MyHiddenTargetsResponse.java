package com.tripmoa.report.dto;

import java.util.List;

public record MyHiddenTargetsResponse(
        String location,
        List<Long> targetIds
) {
}
