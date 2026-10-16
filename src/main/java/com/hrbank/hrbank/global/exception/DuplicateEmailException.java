package com.hrbank.hrbank.global.exception;

public class DuplicateEmailException extends BadRequestException {
    public DuplicateEmailException(String email ) {
        super("이미 존재하는 이메일입니다: " + email );
    }
}
