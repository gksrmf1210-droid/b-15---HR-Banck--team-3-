package com.hrbank.hrbank.changelog.dto.request;

import com.hrbank.hrbank.changelog.entity.ChangeLog.ChangeType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Sort;

import java.time.Instant;

public record ChangeLogSearchRequest(
        String employeeNumber,
        String memo,
        String ipAddress,
        Instant atFrom,
        Instant atTo,
        ChangeType type,
        @Pattern(regexp = "ipAddress|createdAt") String sortField,
        Sort.Direction sortDirection,
        @Positive Long after,
        @Min(1) Integer size
) {
    public ChangeLogSearchRequest {
        // 프론트의 수정일 정렬 키(at)를 엔티티 필드명으로 변환한다.
        sortField = sortField == null || "at".equals(sortField) ? "createdAt" : sortField;
        sortDirection = sortDirection == null ? Sort.Direction.DESC : sortDirection;
        size = size == null ? 20 : size;
    }

    @AssertTrue(message = "시작 시간은 종료 시간보다 늦을 수 없습니다.")
    public boolean isTimeRangeValid() {
        return atFrom == null || atTo == null || !atFrom.isAfter(atTo);
    }
}
