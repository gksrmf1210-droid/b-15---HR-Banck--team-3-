package com.hrbank.hrbank.employee.controller;
import com.hrbank.hrbank.employee.dto.*;
import com.hrbank.hrbank.employee.entity.EmployeeStatus;
import com.hrbank.hrbank.employee.service.EmployeeService;
import com.hrbank.hrbank.global.dto.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeDto> create(
            @RequestPart("employee") @Valid EmployeeCreateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile){
        return ResponseEntity.ok(employeeService.create(request, profile));
    }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> findById(
            @PathVariable Long id){
        return ResponseEntity.ok(employeeService.findById(id));
    }
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeDto> update(
            @PathVariable Long id,
            @RequestPart("employee") @Valid EmployeeUpdateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {
        return ResponseEntity.ok(employeeService.update(id, request, profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<CursorPageResponse<EmployeeDto>> findAll(
            @RequestParam(required = false) String nameOrEmail,
            @RequestParam(required = false) String employeeNumber,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String position,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate hireDateFrom,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate hireDateTo,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(employeeService.findAll(
                nameOrEmail, employeeNumber, departmentName, position, hireDateFrom, hireDateTo, status,idAfter,
                cursor, size, sortField ,  sortDirection));
    }
    @GetMapping("/count")
    public ResponseEntity<Long> count(
            // status, fromDate, toDate 3개
            // 날짜 2개엔 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false)EmployeeStatus status,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false)LocalDate fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false)LocalDate toDate
            ) {
        return ResponseEntity.ok(employeeService.count(status,fromDate,toDate));
    }
    @GetMapping("/stats/distribution")
    public ResponseEntity<List<EmployeeDistributionDto>> distribution(
            @RequestParam(defaultValue = "department") String groupBy,
            @RequestParam(defaultValue = "ACTIVE") EmployeeStatus status
            ) {
        return ResponseEntity.ok(employeeService.distribution(groupBy,status));
    }
    @GetMapping("/stats/trend")
    public ResponseEntity<List<EmployeeTrendDto>> trend(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "month") String unit) {
        return ResponseEntity.ok(employeeService.trend(from, to, unit));
    }

}
