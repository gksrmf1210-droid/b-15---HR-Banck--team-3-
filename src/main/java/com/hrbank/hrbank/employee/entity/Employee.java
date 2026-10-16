package com.hrbank.hrbank.employee.entity;

import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, unique = true, length = 40)
    private String email;

    @Column(nullable = false, updatable = false, unique = true, length = 40)
    private String employeeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = false)
    private LocalDate hireDate;

    public Employee(String name, String email, String employeeNumber, Department department, String position,LocalDate hireDate){
        this.name =name;
        this.email = email;
         this.employeeNumber = employeeNumber;
        this.department = department;
        this.position = position;
        this.status = EmployeeStatus.ACTIVE;
        this.hireDate = hireDate;
    }
    public void update(String name, String email,EmployeeStatus status, Department department, String position,LocalDate hireDate){
        this.name =name;
        this.email = email;
        this.department = department;
        this.position = position;
        this.status = status;
        this.hireDate = hireDate;
    }
}
