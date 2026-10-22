package com.hrbank.hrbank.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
@Getter
@AllArgsConstructor
public class EmployeeTrendDto {
    private LocalDate date;
    private Long count;
    private Long change;
    private Double changeRate;

}
