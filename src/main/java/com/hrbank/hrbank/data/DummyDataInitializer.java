package com.hrbank.hrbank.data;


import com.hrbank.hrbank.department.entity.Department;
import com.hrbank.hrbank.department.repository.DepartmentRepository;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.employee.service.EmployeeNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.entity.EmployeeStatus;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {


    private final DepartmentRepository departmentRepository;
    private final EmployeeNumberGenerator employeeNumberGenerator;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (departmentRepository.count() > 0 ) {
            return;
        }
        List<Department> departments = List.of(
                new Department("개발팀","서비스 개발 및 유지보수", LocalDate.of(2020,1,1)),
                new Department("인사팀", "인사 및 직원 관리", LocalDate.of(2020, 2, 1)),
                new Department("디자인팀", "UI 및 UX 디자인", LocalDate.of(2021, 3, 1)),
                new Department("영업팀", "고객 관리 및 영업 활동", LocalDate.of(2021, 5, 1)),
                new Department("경영지원팀", "회사 운영 및 경영 지원", LocalDate.of(2022, 1, 1))

        );

        departmentRepository.saveAll(departments);

        createEmployee(
                "김민수", "minsu@example.com", departments.get(0),
                "백엔드 개발자", LocalDate.of(2023, 3, 1), EmployeeStatus.ACTIVE);
        createEmployee(
                "이서연", "seoyeon@example.com", departments.get(0),
                "프론트엔드 개발자", LocalDate.of(2024, 6, 10), EmployeeStatus.ACTIVE);
        createEmployee(
                "박지훈", "jihoon@example.com", departments.get(0),
                "서버 개발자", LocalDate.of(2022, 8, 15), EmployeeStatus.ON_LEAVE);
        createEmployee(
                "최유진", "yujin@example.com", departments.get(0),
                "개발 팀장", LocalDate.of(2020, 2, 3), EmployeeStatus.ACTIVE);
        createEmployee(
                "정하늘", "haneul@example.com", departments.get(1),
                "인사 담당자", LocalDate.of(2022, 4, 1), EmployeeStatus.ACTIVE);
        createEmployee(
                "강민지", "minji@example.com", departments.get(1),
                "채용 담당자", LocalDate.of(2023, 7, 10), EmployeeStatus.ACTIVE);
        createEmployee(
                "윤도현", "dohyun@example.com", departments.get(1),
                "인사 매니저", LocalDate.of(2021, 1, 11), EmployeeStatus.RESIGNED);
        createEmployee(
                "한예린", "yerin@example.com", departments.get(1),
                "교육 담당자", LocalDate.of(2024, 3, 4), EmployeeStatus.ACTIVE);
        createEmployee(
                "오지민", "jimin@example.com", departments.get(2),
                "UI 디자이너", LocalDate.of(2023, 5, 8), EmployeeStatus.ACTIVE);
        createEmployee(
                "임서준", "seojun@example.com", departments.get(2),
                "UX 디자이너", LocalDate.of(2022, 9, 1), EmployeeStatus.ACTIVE);
        createEmployee(
                "신채원", "chaewon@example.com", departments.get(2),
                "브랜드 디자이너", LocalDate.of(2024, 1, 15), EmployeeStatus.ON_LEAVE);
        createEmployee(
                "조현우", "hyunwoo@example.com", departments.get(2),
                "디자인 팀장", LocalDate.of(2020, 6, 1), EmployeeStatus.ACTIVE);
        createEmployee(
                "서수빈", "subin@example.com", departments.get(3),
                "영업 담당자", LocalDate.of(2022, 2, 14), EmployeeStatus.ACTIVE);
        createEmployee(
                "권도윤", "doyun@example.com", departments.get(3),
                "고객 관리 담당자", LocalDate.of(2023, 8, 21), EmployeeStatus.ACTIVE);
        createEmployee(
                "황지아", "jia@example.com", departments.get(3),
                "영업 매니저", LocalDate.of(2021, 11, 1), EmployeeStatus.RESIGNED
        );
        createEmployee(
                "안준호", "junho@example.com", departments.get(3),
                "사업 개발 담당자", LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        createEmployee(
                "송아린", "arin@example.com", departments.get(4),
                "회계 담당자", LocalDate.of(2022, 7, 1), EmployeeStatus.ACTIVE);
        createEmployee(
                "문태양", "taeyang@example.com", departments.get(4),
                "총무 담당자", LocalDate.of(2023, 9, 11), EmployeeStatus.ACTIVE);
        createEmployee(
                "백소희", "sohee@example.com", departments.get(4), "경영지원 담당자", LocalDate.of(2024, 2, 1), EmployeeStatus.ON_LEAVE);
        createEmployee(
                "장우진", "woojin@example.com", departments.get(4),
                "경영지원 팀장", LocalDate.of(2020, 4, 6), EmployeeStatus.ACTIVE);



    }
    private void createEmployee(
            String name,
            String email,
            Department department,
            String position,
            LocalDate hireDate,
            EmployeeStatus status

    ) {
        String employeeNumber = employeeNumberGenerator.generate(hireDate);

        Employee employee = new Employee(
                name,
                email,
                employeeNumber,
                department,
                position,
                hireDate
        );

        employee.update(
                name,
                email,
                status,
                department,
                position,
                hireDate
        );
        employeeRepository.save(employee);
    }
}
