package com.hrbank.hrbank.employee.service;


import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.dto.EmployeeCreateRequest;
import com.hrbank.hrbank.employee.dto.EmployeeDto;
import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.entity.EmployeeStatus;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.global.dto.CursorPageResponse;
import com.hrbank.hrbank.global.exception.DepartmentNotFoundException;
import com.hrbank.hrbank.global.exception.DuplicateEmailException;
import com.hrbank.hrbank.global.exception.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.hrbank.hrbank.employee.dto.EmployeeUpdateRequest;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeNumberGenerator employeeNumberGenerator;

    public EmployeeDto create(EmployeeCreateRequest request, MultipartFile profile) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));

        String employeeNumber = employeeNumberGenerator.generate(request.getHireDate());
        Employee employee = new Employee(request.getName(), request.getEmail(), employeeNumber, department, request.getPosition(), request.getHireDate());
        employeeRepository.save(employee);
        return EmployeeDto.from(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id) {
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

    @Transactional(readOnly = true)
    public CursorPageResponse<EmployeeDto> findAll(String nameOrEmail, String employeeNumber, String departmentName, String position,
                                                   LocalDate hireDateFrom, LocalDate hireDateTo, EmployeeStatus status, Long idAfter,
                                                   String cursor, int size, String sortField, String sortDirection) {

        String keyword1 = (nameOrEmail == null) ? "" : nameOrEmail;
        String keyword2 = (departmentName == null) ? "" : departmentName;
        String keyword3 = (position == null) ? "" : position;
        String keyword4 = (employeeNumber == null) ? "" : employeeNumber;
        Pageable limit = PageRequest.of(0, size + 1);   // LIMIT size+1 용도로만 사용
        boolean desc = "desc".equalsIgnoreCase(sortDirection);
        List<Employee> employees;

        Long lastId = idAfter;
        if (lastId == null && cursor != null && !cursor.isBlank()) {
            lastId = Long.parseLong(cursor);
        }
        Employee lastEmployee = (lastId == null) ? null : employeeRepository.findById(lastId).orElse(null);
        if (lastEmployee == null) {
            lastId = null;
        }

        if ("name".equals(sortField)) {
            String nameCursor = (lastEmployee == null) ? null : lastEmployee.getName();
            employees = desc ? employeeRepository.findNameDesc(keyword1, keyword2, keyword3, keyword4,
                    hireDateFrom, hireDateTo, status, nameCursor, lastId, limit)
                    : employeeRepository.findNameAsc(keyword1, keyword2, keyword3, keyword4,
                    hireDateFrom, hireDateTo, status, nameCursor, lastId, limit);
        } else if ("employeeNumber".equals((sortField))) {
            String numberCursor = (lastEmployee == null) ? null : lastEmployee.getEmployeeNumber();
            employees = desc ? employeeRepository.findEmployeeNumberDesc(keyword1, keyword2, keyword3, keyword4,
                    hireDateFrom, hireDateTo, status, numberCursor, lastId, limit)
                    : employeeRepository.findEmployeeNumberAsc(keyword1, keyword2, keyword3, keyword4,
                    hireDateFrom, hireDateTo, status, numberCursor, lastId, limit);
        } else {
            LocalDate dateCursor = (lastEmployee == null) ? null : lastEmployee.getHireDate();
            employees = desc ? employeeRepository.findHireDateDesc(keyword1, keyword2, keyword3, keyword4,
                    hireDateFrom, hireDateTo, status, dateCursor, lastId, limit)
                    : employeeRepository.findHireDateAsc(keyword1, keyword2, keyword3, keyword4,
                    hireDateFrom, hireDateTo, status, dateCursor, lastId, limit);
        }

        boolean hasNext = employees.size() > size;
        if (hasNext) {
            employees = employees.subList(0, size);
        }
        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext) {
            Employee lastOfPage = employees.get(employees.size() - 1);
            nextIdAfter = lastOfPage.getId();
            nextCursor = String.valueOf(nextIdAfter);
        }

        List<EmployeeDto> content = employees.stream()
                .map(e -> EmployeeDto.from(e))
                .toList();


        long totalElements = employeeRepository.countBySearch(
                keyword1, keyword2, keyword3, keyword4, hireDateFrom, hireDateTo, status);


        return new CursorPageResponse<>(content, nextCursor, nextIdAfter, content.size(), totalElements, hasNext);
    }
}

