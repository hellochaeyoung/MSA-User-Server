package com.example.userservice.exp.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    BAD_REQUEST_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Refresh token, do login again"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Expired Authentication, do login again"),

    BAD_REQUEST_LOGIN(HttpStatus.BAD_REQUEST, "Bad Request ID or Password")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
