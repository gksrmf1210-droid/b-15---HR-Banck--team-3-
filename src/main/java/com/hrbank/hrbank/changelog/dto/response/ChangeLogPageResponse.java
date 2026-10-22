package com.hrbank.hrbank.changelog.dto.response;

import java.util.List;

public record ChangeLogPageResponse(
    List<ChangeLogResponse> content,
    Long nextCursor,
    boolean hasNext,
    long totalElements
) {
    public ChangeLogPageResponse(List<ChangeLogResponse> data, Long nextCursor, boolean hasNext) {
        this(data, nextCursor, hasNext, data != null ? data.size() : 0L);
    }
}