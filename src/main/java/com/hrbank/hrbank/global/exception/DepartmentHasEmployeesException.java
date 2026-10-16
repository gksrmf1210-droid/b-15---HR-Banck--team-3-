package com.hrbank.hrbank.global.exception;

public class DepartmentHasEmployeesException extends BadRequestException {
    public DepartmentHasEmployeesException(Long id) {
        super("소속 직원이 있는 부서는 삭제할 수 없습니다: " + id);
    }
}
