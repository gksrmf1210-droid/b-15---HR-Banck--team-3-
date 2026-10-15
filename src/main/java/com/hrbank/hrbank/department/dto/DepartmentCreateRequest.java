package com.hrbank.hrbank.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DepartmentCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private LocalDate establishedDate;
}
