package com.hrbank.hrbank.changelog.service;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogPageResponse;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogResponse;
import com.hrbank.hrbank.changelog.entity.ChangeLog;
import com.hrbank.hrbank.changelog.repository.ChangeLogRepository;
import com.hrbank.hrbank.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChangeLogService {
    private final ChangeLogRepository changeLogRepository;

    @Transactional(readOnly = true)
    public ChangeLogPageResponse search(ChangeLogSearchRequest request) {
        List<ChangeLogResponse> results = changeLogRepository.search(request);
        boolean hasNext = results.size() > request.size();
        List<ChangeLogResponse> content = List.copyOf(
                hasNext ? results.subList(0, request.size()) : results);
        Long nextAfter = hasNext ? content.get(content.size() - 1).id() : null;
        return new ChangeLogPageResponse(content, nextAfter, hasNext, changeLogRepository.countMatching(request));
    }

    // 이력 상세 조회(GET /api/change-logs/{id})의 응답
    @Transactional(readOnly = true)
    public Map<String, Object> getId(Long id) {
        ChangeLog log = changeLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력을 찾을 수 없습니다."));
        var diffs = log.getDetails();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", log.getId());
        response.put("employeeNumber", log.getEmployeeNumber());
        response.put("memo", log.getMemo());
        response.put("ipAddress", log.getIpAddress());
        response.put("createdAt", log.getCreatedAt());
        response.put("at", log.getCreatedAt());
        response.put("type", log.getType());
        response.put("diffs", diffs.stream().map(diff -> {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("propertyName", frontendPropertyName(diff.getPropertyName()));
            values.put("before", diff.getBeforeValue());
            values.put("after", diff.getAfterValue());
            return values;
        }).toList());
        // 삭제된 직원도 저장된 이름 변경 내역으로 표시할 수 있다.
        String employeeName = diffs.stream()
                .filter(diff -> "name".equals(frontendPropertyName(diff.getPropertyName())))
                .map(diff -> diff.getAfterValue() != null ? diff.getAfterValue() : diff.getBeforeValue())
                .filter(Objects::nonNull).findFirst().orElse(null);
        var employee = changeLogRepository.findEmployeeForDetail(log.getEmployeeNumber());
        response.put("employeeName", employeeName != null ? employeeName
                : employee.map(Employee::getName).orElse(null));
        response.put("profileImageId", employee.filter(emp -> emp.getProfileImage() != null)
                .map(emp -> emp.getProfileImage().getId()).orElse(null));
        return response;
    }

    private String frontendPropertyName(String propertyName) {
        return switch (propertyName) {
            case "이름" -> "name";
            case "이메일" -> "email";
            case "부서", "departmentName" -> "department";
            case "직함" -> "position";
            case "입사일" -> "hireDate";
            case "사번" -> "employeeNumber";
            case "상태" -> "status";
            default -> propertyName;
        };
    }

    @Transactional(readOnly = true)
    public Long getChangeLogsCount(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime to = toDate == null ? LocalDateTime.now() : toDate;
        LocalDateTime from = fromDate == null ? to.minusWeeks(1) : fromDate;
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 시간은 종료 시간보다 늦을 수 없습니다.");
        }

        // 시간대 없는 요청은 서버 기본 시간대로 해석한 뒤 저장된 Instant와 비교한다.
        ZoneId zone = ZoneId.systemDefault();
        return changeLogRepository.countByCreatedAtBetween(
                from.atZone(zone).toInstant(), to.atZone(zone).toInstant());
    }
}