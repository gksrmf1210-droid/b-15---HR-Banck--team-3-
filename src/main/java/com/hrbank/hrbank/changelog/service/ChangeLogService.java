package com.hrbank.hrbank.changelog.service;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogDiffResponse;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogPageResponse;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogResponse;
import com.hrbank.hrbank.changelog.entity.ChangeLog;
import com.hrbank.hrbank.changelog.repository.ChangeLogDiffRepository;
import com.hrbank.hrbank.changelog.repository.ChangeLogRepository;
import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.employee.dto.EmployeeUpdateRequest;
import com.hrbank.hrbank.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChangeLogService {
    private final ChangeLogRepository changeLogRepository;
    private final ChangeLogDiffRepository changeLogDiffRepository;

    // ===== 조회 =====

    @Transactional(readOnly = true)
    public ChangeLogPageResponse search(ChangeLogSearchRequest request) {
        List<ChangeLogResponse> results = changeLogRepository.search(request);
        boolean hasNext = results.size() > request.size();
        List<ChangeLogResponse> content = List.copyOf(
                hasNext ? results.subList(0, request.size()) : results);
        Long nextAfter = hasNext ? content.get(content.size() - 1).id() : null;
        return new ChangeLogPageResponse(content, nextAfter, hasNext, changeLogRepository.countMatching(request));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getId(Long id) {
        ChangeLog log = changeLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력을 찾을 수 없습니다."));
        List<ChangeLogDiffResponse> diffs = changeLogDiffRepository.findDiffs(id);
        var employee = changeLogRepository.findEmployeeForDetail(log.getEmployeeNumber());

        String employeeName = diffs.stream()
                .filter(d -> "name".equals(d.propertyName()))
                .map(d -> d.after() != null ? d.after() : d.before())
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> employee.map(Employee::getName).orElse(null));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", log.getId());
        response.put("type", log.getType());
        response.put("employeeNumber", log.getEmployeeNumber());
        response.put("employeeName", employeeName);
        response.put("profileImageId", employee.map(Employee::getProfileImage)
                .map(img -> img.getId()).orElse(null));
        response.put("memo", log.getMemo());
        response.put("ipAddress", log.getIpAddress());
        response.put("at", log.getCreatedAt());
        response.put("diffs", diffs);
        return response;
    }

    @Transactional(readOnly = true)
    public Long getChangeLogsCount(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime to = toDate == null ? LocalDateTime.now() : toDate;
        LocalDateTime from = fromDate == null ? to.minusWeeks(1) : fromDate;
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 시간은 종료 시간보다 늦을 수 없습니다.");
        }
        ZoneId zone = ZoneId.systemDefault();
        return changeLogRepository.countByCreatedAtBetween(
                from.atZone(zone).toInstant(), to.atZone(zone).toInstant());
    }

    // ===== 이력 등록 (직원 서비스가 호출) =====

    @Transactional
    public void recordCreate(Employee e, String memo, String ip) {
        save(ChangeLog.ChangeType.CREATED, e.getEmployeeNumber(), memo, ip, Map.of(), snapshot(e));
    }

    @Transactional
    public void recordUpdate(Employee e, EmployeeUpdateRequest r, Department newDepartment, String ip) {
        save(ChangeLog.ChangeType.UPDATED, e.getEmployeeNumber(), r.getMemo(), ip, snapshot(e),
                snapshot(r.getName(), r.getEmail(), newDepartment.getName(),
                        r.getPosition(), r.getHireDate(), r.getStatus()));
    }

    @Transactional
    public void recordDelete(Employee e, String ip) {
        save(ChangeLog.ChangeType.DELETED, e.getEmployeeNumber(), null, ip, snapshot(e), Map.of());
    }

    private void save(ChangeLog.ChangeType type, String employeeNumber, String memo, String ip,
                      Map<String, String> before, Map<String, String> after) {
        ChangeLog log = new ChangeLog(type, employeeNumber, memo, ip);
        Set<String> keys = new LinkedHashSet<>(before.keySet());
        keys.addAll(after.keySet());
        for (String key : keys) {
            String b = before.get(key);
            String a = after.get(key);
            if (!Objects.equals(b, a)) {
                log.addDetail(key, b, a);
            }
        }
        if (type == ChangeLog.ChangeType.UPDATED && log.getDetails().isEmpty()) {
            return;
        }
        changeLogRepository.save(log);
    }

    private Map<String, String> snapshot(Employee e) {
        return snapshot(e.getName(), e.getEmail(),
                e.getDepartment() == null ? null : e.getDepartment().getName(),
                e.getPosition(), e.getHireDate(), e.getStatus());
    }

    private Map<String, String> snapshot(String name, String email, String department,
                                         String position, LocalDate hireDate, Object status) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("email", email);
        m.put("department", department);
        m.put("position", position);
        m.put("hireDate", Objects.toString(hireDate, null));
        m.put("status", Objects.toString(status, null));
        return m;
    }
}