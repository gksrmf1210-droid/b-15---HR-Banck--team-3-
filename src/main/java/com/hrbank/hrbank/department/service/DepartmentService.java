package com.hrbank.hrbank.department.service;

import com.hrbank.hrbank.department.dto.DepartmentCreateRequest;
import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.dto.DepartmentUpdateRequest;
import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.global.dto.CursorPageResponse;
import com.hrbank.hrbank.global.exception.DepartmentHasEmployeesException;
import com.hrbank.hrbank.global.exception.DepartmentNotFoundException;
import com.hrbank.hrbank.global.exception.DuplicateDepartmentNameException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentDto create(DepartmentCreateRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateDepartmentNameException(request.getName());
        }
        Department department = new Department(request.getName(), request.getDescription(), request.getEstablishedDate());
        return DepartmentDto.from(departmentRepository.save(department), 0L);
    }

    public DepartmentDto update(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateDepartmentNameException(request.getName());
        }
        department.update(request.getName(), request.getDescription(), request.getEstablishedDate());
        Long count = employeeRepository.countByDepartmentId(id);
        return DepartmentDto.from(department, count);
    }

    @Transactional(readOnly = true)
    public DepartmentDto findById(Long id) {
        Department department = departmentRepository.findById(id).
                orElseThrow(() -> new DepartmentNotFoundException(id));
        Long count = employeeRepository.countByDepartmentId(id);
        return DepartmentDto.from(department, count);
    }

    public void delete(Long id) {
        Department department = departmentRepository.findById(id).
                orElseThrow(() -> new DepartmentNotFoundException(id));
        if (employeeRepository.countByDepartmentId(id) > 0) {
            throw new DepartmentHasEmployeesException(id);
        }
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DepartmentDto> findAll(String nameOrDescription, Long idAfter, String cursor,
                                                     int size, String sortField, String sortDirection) {
        String keyword = (nameOrDescription == null) ? "" : nameOrDescription;
        Pageable limit = PageRequest.of(0, size + 1);   // LIMIT size+1 용도로만 사용
        boolean desc = "desc".equalsIgnoreCase(sortDirection);
        List<Department> departments;
        if ("name".equals(sortField)) {
            departments = desc
                    ? departmentRepository.findNameDesc(keyword, cursor, limit)
                    : departmentRepository.findNameAsc(keyword, cursor, limit);
        } else {
            LocalDate dateCursor = (cursor == null) ? null : LocalDate.parse(cursor);
            departments = desc
                    ? departmentRepository.findDateDesc(keyword, dateCursor, idAfter, limit)
                    : departmentRepository.findDateAsc(keyword, dateCursor, idAfter, limit);
        }
        boolean hasNext = departments.size() > size;
        if (hasNext) {
            departments = departments.subList(0, size);
        }

        List<DepartmentDto> content = departments.stream()
                .map(d -> DepartmentDto.from(d, employeeRepository.countByDepartmentId(d.getId())))
                .toList();

        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext) {
            Department last = departments.get(departments.size() - 1);
            nextCursor = "name".equals(sortField) ? last.getName() : last.getEstablishedDate().toString();
            nextIdAfter = last.getId();
        }

        long totalElements = departmentRepository.countByNameContainingOrDescriptionContaining(keyword, keyword);
        return new CursorPageResponse<>(content, nextCursor, nextIdAfter, content.size(), totalElements, hasNext);
    }
}
