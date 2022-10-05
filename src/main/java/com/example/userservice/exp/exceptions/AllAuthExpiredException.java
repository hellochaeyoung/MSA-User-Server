package com.example.userservice.exp.exceptions;

import com.example.userservice.exp.codes.ErrorCode;
import lombok.Getter;

@Getter
public class AllAuthExpiredException extends CustomException {

    public AllAuthExpiredException(String message,
        ErrorCode errorCode) {
        super(message, errorCode);
    }
}
