package com.hrbank.hrbank.department.dto;

import com.hrbank.hrbank.department.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DepartmentDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate establishedDate;
    private Long employeeCount;
    public static DepartmentDto from(Department department, Long employeeCount){
        return new DepartmentDto(department.getId(), department.getName(),department.getDescription(),
                department.getEstablishedDate(),employeeCount);

    }
}
