package com.hrbank.hrbank.employee.repository;

import com.hrbank.hrbank.employee.entity.Employee;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  boolean existsByUpdatedAtAfter(LocalDateTime lastCompletedAt);

  @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.id > :lastId ORDER BY e.id ASC")
  List<Employee> findEmployeesChunk(@Param("lastId") Long lastId, Pageable pageable);
}