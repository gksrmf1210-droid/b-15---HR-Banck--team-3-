package com.hrbank.hrbank.changelog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hrbank.hrbank.changelog.entity.ChangeLog.ChangeType;

import java.time.Instant;

public record ChangeLogResponse(
        Long id,
        String employeeNumber,
        String memo,
        String ipAddress,
        Instant createdAt,
        ChangeType type
) {
    @JsonProperty("at")
    public Instant at() {
        return createdAt;
    }
}
