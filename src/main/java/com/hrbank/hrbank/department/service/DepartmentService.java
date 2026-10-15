package com.hrbank.hrbank.department.service;

import com.hrbank.hrbank.department.dto.DepartmentCreateRequest;
import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.dto.DepartmentUpdateRequest;
import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
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
            throw new IllegalArgumentException("이미 있는 부서입니다: " + request.getName());
        }
        Department department = new Department(request.getName(), request.getDescription(), request.getEstablishedDate());
        return DepartmentDto.from(departmentRepository.save(department), 0L);
    }
    public DepartmentDto update(DepartmentUpdateRequest request){

    }
}
