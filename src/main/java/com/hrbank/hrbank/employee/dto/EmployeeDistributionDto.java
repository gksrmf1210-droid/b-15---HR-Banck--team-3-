package com.hrbank.hrbank.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@AllArgsConstructor
public class EmployeeDistributionDto {
    private String groupKey;
    private Long count;
    private Double percentage;
    public static EmployeeDistributionDto from(Object[] row, long total){
        String groupKey = (String) row[0];
        Long count = (Long) row[1];
        Double percentage = (total == 0) ? 0 : 100.0 * count / total;
        return new EmployeeDistributionDto(groupKey, count, percentage);
    }

}
