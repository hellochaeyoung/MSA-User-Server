package com.example.userservice.exp.exceptions;

import com.example.userservice.exp.codes.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private ErrorCode errorCode;

    public CustomException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
