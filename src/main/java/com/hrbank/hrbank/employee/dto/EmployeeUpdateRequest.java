package com.hrbank.hrbank.employee.dto;

import com.hrbank.hrbank.employee.entity.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EmployeeUpdateRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotNull
    private Long departmentId;
    @NotBlank
    private String position;
    @NotNull
    private LocalDate hireDate;
    @NotNull
    private EmployeeStatus status;
    private String memo;
}