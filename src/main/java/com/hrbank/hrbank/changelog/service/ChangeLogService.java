package com.hrbank.hrbank.changelog.service;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogRequest;
import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogPageResponse;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogResponse;
import com.hrbank.hrbank.changelog.entity.ChangeLog;
import com.hrbank.hrbank.changelog.repository.ChangeLogDiffRepository;
import com.hrbank.hrbank.changelog.repository.ChangeLogRepository;
import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;
    private final ChangeLogDiffRepository changeLogDiffRepository;

    @Transactional(readOnly = true)
    public ChangeLogPageResponse search(ChangeLogSearchRequest request) {
        List<ChangeLogResponse> results = changeLogRepository.search(request);
        boolean hasNext = results.size() > request.size();
        List<ChangeLogResponse> content = List.copyOf(
                hasNext ? results.subList(0, request.size()) : results);
        Long nextAfter = hasNext ? content.get(content.size() - 1).id() : null;
        return new ChangeLogPageResponse(content, nextAfter, hasNext);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getId(Long id) {
        ChangeLog log = changeLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력을 찾을 수 없습니다."));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", log.getId());
        response.put("employeeNumber", log.getEmployeeNumber());
        response.put("memo", log.getMemo());
        response.put("ipAddress", log.getIpAddress());
        response.put("createdAt", log.getCreatedAt());
        response.put("type", log.getType());
        response.put("details", changeLogDiffRepository.findByChangeLogId(id).stream().map(diff -> {
            // 추가·삭제 이력은 변경 전 또는 변경 후 값이 null일 수 있다.
            Map<String, String> values = new LinkedHashMap<>();
            values.put("propertyName", diff.getPropertyName());
            values.put("beforeValue", diff.getBeforeValue());
            values.put("afterValue", diff.getAfterValue());
            return values;
        }).toList());
        return response;
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

    @Transactional
    public Long create(ChangeLogRequest request, String memo, String ipAddress) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "직원을 찾을 수 없습니다. id=" + request.getEmployeeId()));

        ChangeLog log = new ChangeLog(ChangeLog.ChangeType.CREATED, employee.getEmployeeNumber(), memo, ipAddress);

        // 추가는 수정 전 값이 없으므로 값이 있는 속성만 before=null로 저장
        request.toPropertyMap().forEach((key, value) -> {
            if (value != null) {
                log.addDetail(key, null, value);
            }
        });

        return changeLogRepository.save(log).getId();
    }



    @Transactional
    public void recordUpdate(String employeeNumber, ChangeLogRequest request,
                             String memo, String ipAddress) {

        Employee emp = employeeRepository.findTopByEmployeeNumberStartingWithOrderByEmployeeNumberDesc(employeeNumber)
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        Map<String, String> beforeMap = toSnapshot(emp);                  // 직원 변경 전 값
        Map<String, String> afterMap = request.toPropertyMap();            // 요청 값

        ChangeLog log = new ChangeLog(ChangeLog.ChangeType.UPDATED, employeeNumber, memo, ipAddress);

        for (String key : beforeMap.keySet()) {
            String b = beforeMap.get(key);
            String a = afterMap.get(key);
            if (!Objects.equals(b, a)) {       // 달라진 속성만
                log.addDetail(key, b, a);
            }
        }

        if (log.getDetails().isEmpty()) {        // 달라진 게 없으면 저장 안 함
            return;
        }
        changeLogRepository.save(log);
    }

    private Map<String, String> toSnapshot(Employee employee) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("이름", employee.getName());
        properties.put("이메일", employee.getEmail());
        properties.put("부서", employee.getDepartment() == null
                ? null : employee.getDepartment().getName());
        properties.put("직함", employee.getPosition());
        properties.put("입사일", employee.getHireDate() == null
                ? null : employee.getHireDate().toString());
        return properties;
    }


}
