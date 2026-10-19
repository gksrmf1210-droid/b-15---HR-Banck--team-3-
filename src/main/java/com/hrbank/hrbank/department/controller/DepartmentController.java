package com.hrbank.hrbank.department.controller;
import com.hrbank.hrbank.department.dto.DepartmentCreateRequest;
import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.dto.DepartmentUpdateRequest;
import com.hrbank.hrbank.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hrbank.hrbank.global.dto.CursorPageResponse;

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
    @GetMapping
    public ResponseEntity<CursorPageResponse<DepartmentDto>> findAll(
            @RequestParam(required = false) String nameOrDescription,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "establishedDate") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(departmentService.findAll(
                nameOrDescription, idAfter, cursor, size, sortField, sortDirection));
    }
}
