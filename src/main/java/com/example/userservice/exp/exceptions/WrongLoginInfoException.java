package com.example.userservice.exp.exceptions;

import com.example.userservice.exp.codes.ErrorCode;
import lombok.Getter;

@Getter
public class WrongLoginInfoException extends CustomException {

    public WrongLoginInfoException(String message,
        ErrorCode errorCode) {
        super(message, errorCode);
    }
}
