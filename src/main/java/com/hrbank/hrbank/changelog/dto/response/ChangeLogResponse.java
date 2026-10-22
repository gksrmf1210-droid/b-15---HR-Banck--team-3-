package com.hrbank.hrbank.changelog.dto.response;

import com.hrbank.hrbank.changelog.entity.ChangeLog.ChangeType;

import java.time.Instant;

public record ChangeLogResponse(
        Long id,
        String employeeNumber,
        String memo,
        String ipAddress,
        Instant at,
        ChangeType type
) {}