package com.hrbank.hrbank.department.controller;


import com.hrbank.hrbank.department.dto.DepartmentCreateRequest;
import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.dto.DepartmentUpdateRequest;
import com.hrbank.hrbank.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@RequestBody @Valid DepartmentCreateRequest request){
        return ResponseEntity.ok(departmentService.create(request));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(@PathVariable Long id, @RequestBody @Valid  DepartmentUpdateRequest request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
