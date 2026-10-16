package com.hrbank.hrbank.employee.repository;

import com.hrbank.hrbank.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
