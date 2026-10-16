package com.hrbank.hrbank.changelog.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ChangeLogRequest {
    private String name;            // 직원 이름
    private String email;
    private String departmentName;  // 부서 이름
    private String position;        // 직함
    private LocalDate hireDate;     // 입사일
    private Long profileImageId;    // 프로필 이미지
}
