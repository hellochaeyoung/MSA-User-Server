package com.example.userservice.exp.exceptions;

import com.example.userservice.exp.codes.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends CustomException {

    public InvalidRefreshTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
