package com.example.userservice.exp.handlers;

import com.example.userservice.exp.ErrorResponse;
import com.example.userservice.exp.exceptions.AllAuthExpiredException;
import com.example.userservice.exp.exceptions.InvalidRefreshTokenException;
import com.example.userservice.exp.exceptions.WrongLoginInfoException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler( InvalidRefreshTokenException.class )
    protected ResponseEntity<?> InvalidRefreshTokenException(InvalidRefreshTokenException e) {
        log.error("InvalidRefreshTokenException", e);
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(AllAuthExpiredException.class )
    protected ResponseEntity<?> AllAuthExpiredException(AllAuthExpiredException e) {
        log.error("AllAuthExpiredException", e);
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(WrongLoginInfoException.class )
    protected ResponseEntity<?> WrongLoginInfoException(WrongLoginInfoException e) {
        log.error("WrongLoginInfoException", e);
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getHttpStatus());
    }

}
