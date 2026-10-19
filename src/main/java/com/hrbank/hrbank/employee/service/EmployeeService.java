package com.hrbank.hrbank.employee.service;


import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.dto.EmployeeCreateRequest;
import com.hrbank.hrbank.employee.dto.EmployeeDto;
import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.global.exception.DepartmentNotFoundException;
import com.hrbank.hrbank.global.exception.DuplicateEmailException;
import com.hrbank.hrbank.global.exception.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.hrbank.hrbank.employee.dto.EmployeeUpdateRequest;
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeNumberGenerator employeeNumberGenerator;

    public EmployeeDto create(EmployeeCreateRequest request, MultipartFile profile ){
        if (employeeRepository.existsByEmail(request.getEmail())){
            throw new DuplicateEmailException(request.getEmail());
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));

        String employeeNumber = employeeNumberGenerator.generate(request.getHireDate());
        Employee employee = new Employee(request.getName(), request.getEmail(), employeeNumber,department, request.getPosition(), request.getHireDate());
        employeeRepository.save(employee);
        return EmployeeDto.from(employee);
    }
    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return EmployeeDto.from(employee);
    }
    public EmployeeDto update(Long id, EmployeeUpdateRequest request, MultipartFile profile) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException(request.getEmail());
        }
        // TODO: 수정 이력 — 바꾸기 "전" 값이 필요하므로 이 위치에서 이전 값 기록 (이대원 연동)
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));
        employee.update(request.getName(), request.getEmail(), request.getStatus(),
                department, request.getPosition(), request.getHireDate());
        // TODO: 프로필 이미지 교체 (이준영 연동)
        return EmployeeDto.from(employee);
    }
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        // TODO: 수정 이력 "삭제" 기록 (이대원 연동) — 지우기 전에 기록해야 함
        // TODO: 프로필 이미지 파일 삭제 (이준영 연동)
        employeeRepository.delete(employee);
    }
}
