package com.hrbank.hrbank.department.service;

import com.hrbank.hrbank.department.dto.DepartmentCreateRequest;
import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.dto.DepartmentUpdateRequest;
import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.global.exception.DepartmentNotFoundException;
import com.hrbank.hrbank.global.exception.DuplicateDepartmentNameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public DepartmentDto create(DepartmentCreateRequest request){
        if (departmentRepository.existsByName(request.getName())){
            throw new DuplicateDepartmentNameException(request.getName());
        }
        Department department = new Department(request.getName(), request.getDescription(), request.getEstablishedDate());
        return DepartmentDto.from(departmentRepository.save(department), 0L);
    }
    public DepartmentDto update(Long id, DepartmentUpdateRequest request){
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)){
            throw new DuplicateDepartmentNameException(request.getName());
        }
        department.update(request.getName(), request.getDescription(), request.getEstablishedDate());
        Long count = employeeRepository.countByDepartmentId(id);
        return DepartmentDto.from(department, count);
    }
}
