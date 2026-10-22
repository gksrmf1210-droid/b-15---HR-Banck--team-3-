package com.hrbank.hrbank.employee.dto;

import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.entity.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;


@Getter
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String name;
    private String email;
    private String employeeNumber;
    private Long departmentId;
    private String departmentName;
    private String position;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Long profileImageId;

    public static EmployeeDto from(Employee employee) {
        Long profileImageId = (employee.getProfileImage() == null) ? null : employee.getProfileImage().getId();
        return new EmployeeDto(employee.getId(), employee.getName(),
                employee.getEmail(), employee.getEmployeeNumber(),
                employee.getDepartment().getId(), employee.getDepartment().getName(),
                employee.getPosition(), employee.getHireDate(), employee.getStatus(), profileImageId);
    }
}