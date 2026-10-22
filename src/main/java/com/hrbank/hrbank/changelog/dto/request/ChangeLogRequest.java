//package com.hrbank.hrbank.changelog.dto.request;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDate;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Getter
//@NoArgsConstructor
//public class ChangeLogRequest {
//    private String name;            // 직원 이름
//    private String email;
//    private String departmentName;  // 부서 이름
//    private String position;        // 직함
//    private LocalDate hireDate;     // 입사일
//    private Long profileImageId;    // 프로필 이미지
//    private Long employeeId;
//
//    public Map<String, String> toPropertyMap() {
//        Map<String, String> properties = new LinkedHashMap<>();
//        properties.put("이름", name);
//        properties.put("이메일", email);
//        properties.put("부서", departmentName);
//        properties.put("직함", position);
//        properties.put("입사일", hireDate == null ? null : hireDate.toString());
//        return properties;
//    }
//}
