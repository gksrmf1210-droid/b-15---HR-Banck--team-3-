package com.hrbank.hrbank.changelog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hrbank.hrbank.changelog.entity.ChangeLog.ChangeType;

import java.time.Instant;

public record ChangeLogResponse(
        Long id,
        String employeeNumber,
        String memo,
        String ipAddress,
        @JsonProperty("at")
        Instant at,
        ChangeType type
) {}