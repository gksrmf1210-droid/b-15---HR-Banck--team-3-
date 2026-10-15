package com.hrbank.hrbank.global.exception;

public class DuplicateDepartmentNameException extends BadRequestException {
    public DuplicateDepartmentNameException(String name) {
        super("이미 존재하는 부서 이름입니다: " + name);
    }
}
