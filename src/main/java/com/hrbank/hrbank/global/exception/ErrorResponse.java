package com.hrbank.hrbank.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String message;
    private String details;
    public static ErrorResponse of(int status, String message, String details){
        return new ErrorResponse(Instant.now(), status, message, details);
    }
}
