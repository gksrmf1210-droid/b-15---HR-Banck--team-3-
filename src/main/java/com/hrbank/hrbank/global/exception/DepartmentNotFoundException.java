package com.hrbank.hrbank.global.exception;

public class DepartmentNotFoundException extends NotFoundException {
    public DepartmentNotFoundException(Long id) {
        super("부서를 찾을 수 없습니다: " + id);
    }
}
