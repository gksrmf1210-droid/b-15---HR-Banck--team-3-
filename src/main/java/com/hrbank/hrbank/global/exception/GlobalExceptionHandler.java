package com.hrbank.hrbank.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "리소스를 찾을 수 없습니다.", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다.", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.CONFLICT.value(), "요청이 현재 상태와 충돌합니다.", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

}