package com.bookingcar.handler;

import com.bookingcar.exceptions.AccessException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@ControllerAdvice(basePackages = "com.bookingcar.controller")
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleException(Exception exception) throws Exception {
        if (exception instanceof ValidationException) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (exception instanceof AccessException) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
        }

        throw exception;
    }
}
