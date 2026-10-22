package com.hrbank.hrbank.employee.service;


import com.hrbank.hrbank.changelog.dto.request.ChangeLogRequest;
import com.hrbank.hrbank.changelog.service.ChangeLogService;
import com.hrbank.hrbank.department.dto.DepartmentDto;
import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.dto.*;
import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.entity.EmployeeStatus;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.file.service.FileService;
import com.hrbank.hrbank.file.entity.FileMeta;
import java.io.IOException;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeNumberGenerator employeeNumberGenerator;
    private final FileService fileService;
    private final ChangeLogService changeLogService;

    public EmployeeDto create(EmployeeCreateRequest request, MultipartFile profile) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));
        String employeeNumber = employeeNumberGenerator.generate(request.getHireDate());
        Employee employee = new Employee(request.getName(), request.getEmail(), employeeNumber, department,
                request.getPosition(), request.getHireDate());
        if (profile != null && !profile.isEmpty()) {
            employee.changeProfileImage(uploadProfile(profile));
        }
        employeeRepository.save(employee);

        changeLogService.recordCreate(employee, "직원 등록", null);


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



        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.getDepartmentId()));

        changeLogService.recordUpdate(employee, request, department, null);

        employee.update(request.getName(), request.getEmail(), request.getStatus(),
                department, request.getPosition(), request.getHireDate());

        if (profile != null && !profile.isEmpty()) {
            FileMeta oldImage = employee.getProfileImage();
            employee.changeProfileImage(uploadProfile(profile));
            if (oldImage != null) {
                deleteFile(oldImage.getId());
            }
        }

        return EmployeeDto.from(employee);
    }

    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        changeLogService.recordDelete(employee, null);
        employeeRepository.delete(employee);
        if (employee.getProfileImage() != null) {
            deleteFile(employee.getProfileImage().getId());
        }
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

    @Transactional(readOnly = true)
    public long count(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate ==null){
            toDate = LocalDate.now();
        }
        long countResult = employeeRepository.countForStats(status, fromDate, toDate);
        return countResult;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDistributionDto> distribution(String groupBy, EmployeeStatus status) {
        List<Object[]> rows;
        long total = employeeRepository.countForStats(status, null, null);
        if ("position".equals(groupBy)){
            rows = employeeRepository.countForPositionStat(status);
        }else {
            rows = employeeRepository.countForDepartmentStat(status);
        }
        return rows.stream()
                .map(row -> EmployeeDistributionDto.from(row, total))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeTrendDto> trend(LocalDate from, LocalDate to, String unit) {
        if (to == null) {
            to = LocalDate.now();
        }
        if (from == null) {
            from = plus(to, unit, -12);
        }
        List<EmployeeTrendDto> result = new ArrayList<>();
        Long prevCount = null;
        LocalDate date = from;

        while (!date.isAfter(to)) {
            long count = employeeRepository.countByHireDateLessThanEqualAndStatusNot(date, EmployeeStatus.RESIGNED);
            long change = (prevCount == null) ? 0 : count - prevCount;
            double changeRate = (prevCount == null || prevCount == 0) ? 0 : change * 100.0 / prevCount;
            result.add(new EmployeeTrendDto(date, count, change, changeRate));
            prevCount = count;
            date = plus(date, unit, 1);
        }
        return result;
    }

    private LocalDate plus(LocalDate date, String unit, long amount) {
        if ("day".equals(unit)) {
            return date.plusDays(amount);
        } else if ("week".equals(unit)) {
            return date.plusWeeks(amount);
        } else if ("quarter".equals(unit)) {
            return date.plusMonths(3 * amount);
        } else if ("year".equals(unit)) {
            return date.plusYears(amount);
        } else {
            return date.plusMonths(amount);
        }
    }

    private FileMeta uploadProfile(MultipartFile profile) {
        try {
            return fileService.upload(profile.getInputStream(), profile.getOriginalFilename(), profile.getContentType(), profile.getSize()
            );
        } catch (IOException e) {
            throw new IllegalStateException("프로필 이미지 업로드에 실패했습니다.", e);
        }
    }
    private void deleteFile(Long fileId) {
        try {
            fileService.delete(fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

