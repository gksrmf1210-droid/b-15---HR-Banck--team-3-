package com.hrbank.hrbank.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DepartmentCreateRequest {
    @NotBlank @Size(max = 40)
    private String name;
    @NotBlank @Size(max = 100)
    private String description;
    @NotNull
    private LocalDate establishedDate;
}
