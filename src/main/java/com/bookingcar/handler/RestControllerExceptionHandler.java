package com.bookingcar.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(basePackages = "com.bookingcar.rest")
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public void handleException(Exception exception) {
        log.error("REST FAILED", exception);
    }
}
