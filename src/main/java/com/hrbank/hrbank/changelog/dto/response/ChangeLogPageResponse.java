package com.hrbank.hrbank.changelog.dto.response;

import java.util.List;

public record ChangeLogPageResponse(
        List<ChangeLogResponse> content,
        Long nextAfter,
        boolean hasNext
) {
}
