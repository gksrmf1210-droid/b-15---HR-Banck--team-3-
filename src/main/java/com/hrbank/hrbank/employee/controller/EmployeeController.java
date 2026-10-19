package com.hrbank.hrbank.employee.controller;
import com.hrbank.hrbank.employee.dto.EmployeeCreateRequest;
import com.hrbank.hrbank.employee.dto.EmployeeDto;
import com.hrbank.hrbank.employee.dto.EmployeeUpdateRequest;
import com.hrbank.hrbank.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
