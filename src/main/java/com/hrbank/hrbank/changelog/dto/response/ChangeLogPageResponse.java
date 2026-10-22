package com.hrbank.hrbank.changelog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChangeLogPageResponse(
        List<ChangeLogResponse> content,
        Long nextAfter,
        boolean hasNext,
        long totalElements
) {
    public ChangeLogPageResponse(List<ChangeLogResponse> content, Long nextAfter, boolean hasNext) {
        this(content, nextAfter, hasNext, content.size());
    }

    @JsonProperty("nextCursor")
    public String nextCursor() {
        return nextAfter == null ? null : nextAfter.toString();
    }

    @JsonProperty("nextIdAfter")
    public Long nextIdAfter() {
        return nextAfter;
    }
}
