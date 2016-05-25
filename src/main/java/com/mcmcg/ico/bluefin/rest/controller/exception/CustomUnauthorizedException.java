package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "You don't have authorize to access resource")
public class CustomUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 7986636575653470830L;

    public CustomUnauthorizedException(String msg) {
        super(msg);
    }
}