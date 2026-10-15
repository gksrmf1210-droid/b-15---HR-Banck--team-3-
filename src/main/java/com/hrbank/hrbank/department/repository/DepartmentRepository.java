package com.hrbank.hrbank.department.repository;

import com.hrbank.hrbank.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
