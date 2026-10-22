package com.hrbank.hrbank.employee.repository;

import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.employee.entity.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.hrbank.hrbank.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
//    Long id(Long id);
//
//    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDepartmentId(Long departmentId);

    Long countByDepartmentId(Long departmentId);

    Optional<Employee> findTopByEmployeeNumberStartingWithOrderByEmployeeNumberDesc(String prefix);


    boolean existsByUpdatedAtAfter(LocalDateTime lastCompletedAt);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.id > :lastId ORDER BY e.id ASC")
    List<Employee> findEmployeesChunk(@Param("lastId") Long lastId, Pageable pageable);

    @Query(""" 

            SELECT e FROM Employee e LEFT JOIN FETCH e.department 
            where (e.name like %:nameOrEmail% or e.email like %:nameOrEmail%) 
            AND e.department.name like %:departmentName%
            AND e.position like %:position%
            AND e.employeeNumber like %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            AND (:lastId IS NULL OR e.name > :cursor OR (e.name = :cursor AND e.id > :lastId))
            ORDER BY e.name ASC,  e.id ASC
            """)
    List<Employee> findNameAsc(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status,
            @Param("cursor") String cursor,
            @Param("lastId") Long lastId,
            Pageable pageable);

    @Query("""
            
            SELECT e FROM Employee e LEFT JOIN FETCH e.department 
            where (e.name like %:nameOrEmail% or e.email like %:nameOrEmail%) 
            AND e.department.name like %:departmentName%
            AND e.position like %:position%
            AND e.employeeNumber like %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            AND (:lastId IS NULL OR e.name < :cursor OR (e.name = :cursor AND e.id < :lastId))
            ORDER BY e.name desc ,  e.id desc 
            """)
    List<Employee> findNameDesc(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status,
            @Param("cursor") String cursor,
            @Param("lastId") Long lastId,
            Pageable pageable);
    @Query("""
            
            SELECT e FROM Employee e LEFT JOIN FETCH e.department 
            where (e.name like %:nameOrEmail% or e.email like %:nameOrEmail%) 
            AND e.department.name like %:departmentName%
            AND e.position like %:position%
            AND e.employeeNumber like %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            AND (:lastId IS NULL OR e.hireDate > :cursor OR (e.hireDate = :cursor AND e.id > :lastId))
            ORDER BY e.hireDate ASC,  e.id ASC
            """)
    List<Employee> findHireDateAsc(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status,
            @Param("cursor") LocalDate cursor,
            @Param("lastId") Long lastId,
            Pageable pageable);
    @Query("""
            
            SELECT e FROM Employee e LEFT JOIN FETCH e.department 
            where (e.name like %:nameOrEmail% or e.email like %:nameOrEmail%) 
            AND e.department.name like %:departmentName%
            AND e.position like %:position%
            AND e.employeeNumber like %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            AND (:lastId IS NULL OR e.hireDate < :cursor OR (e.hireDate = :cursor AND e.id < :lastId))
            ORDER BY e.hireDate desc ,  e.id desc
            """)
    List<Employee> findHireDateDesc(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status,
            @Param("cursor") LocalDate cursor,
            @Param("lastId") Long lastId,
            Pageable pageable);
    @Query("""
            
            SELECT e FROM Employee e LEFT JOIN FETCH e.department 
            where (e.name like %:nameOrEmail% or e.email like %:nameOrEmail%) 
            AND e.department.name like %:departmentName%
            AND e.position like %:position%
            AND e.employeeNumber like %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            AND (:lastId IS NULL OR e.employeeNumber > :cursor OR (e.employeeNumber = :cursor AND e.id > :lastId))
            ORDER BY e.employeeNumber ASC,  e.id ASC
            """)
    List<Employee> findEmployeeNumberAsc(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status,
            @Param("cursor") String cursor,
            @Param("lastId") Long lastId,
            Pageable pageable);

    @Query("""
            
            SELECT e FROM Employee e LEFT JOIN FETCH e.department 
            where (e.name like %:nameOrEmail% or e.email like %:nameOrEmail%) 
            AND e.department.name like %:departmentName%
            AND e.position like %:position%
            AND e.employeeNumber like %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            AND (:lastId IS NULL OR e.employeeNumber < :cursor OR (e.employeeNumber = :cursor AND e.id < :lastId))
            ORDER BY e.employeeNumber DESC,  e.id DESC
            """)
    List<Employee> findEmployeeNumberDesc(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status,
            @Param("cursor") String cursor,
            @Param("lastId") Long lastId,
            Pageable pageable);
    @Query("""
            SELECT COUNT(e) FROM Employee e LEFT JOIN e.department
            WHERE (e.name LIKE %:nameOrEmail% OR e.email LIKE %:nameOrEmail%)
            AND e.department.name LIKE %:departmentName%
            AND e.position LIKE %:position%
            AND e.employeeNumber LIKE %:employeeNumber%
            AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom)
            AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo)
            AND (:status IS NULL OR e.status = :status)
            """)
    long countBySearch(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("employeeNumber") String employeeNumber,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmployeeStatus status);
    }




