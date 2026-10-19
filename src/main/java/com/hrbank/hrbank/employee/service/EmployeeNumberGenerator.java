package com.hrbank.hrbank.employee.service;


import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class EmployeeNumberGenerator {
    private final EmployeeRepository employeeRepository;

    public String generate(LocalDate hireYear) {
        String prefix = "EMP-" + hireYear.getYear() + "-";

        int nxtNumber = employeeRepository.findTopByEmployeeNumberStartingWithOrderByEmployeeNumberDesc(prefix)
                .map(employee -> extractSequence(employee.getEmployeeNumber()) + 1)
                .orElse(1);
        return prefix + String.format("%03d", nxtNumber);
    }
    private int extractSequence(String employeeNumber) {
        String sequence = employeeNumber.substring(employeeNumber.lastIndexOf("-") + 1);
        return Integer.parseInt(sequence);
    }
}
