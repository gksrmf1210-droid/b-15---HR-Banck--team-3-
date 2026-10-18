package com.hrbank.hrbank.employee.repository;

import com.hrbank.hrbank.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByDepartmentId(Long departmentId);
    Long countByDepartmentId(Long departmentId);
    Optional<Employee> findTopByEmployeeNumberStartingWithOrderByEmployeeNumberDesc(String prefix);
}
