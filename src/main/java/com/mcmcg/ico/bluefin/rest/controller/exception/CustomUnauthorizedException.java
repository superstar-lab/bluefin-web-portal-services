package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "An authorization token is required to request this resource.")
public class CustomUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = -2125288439743361226L;

    public CustomUnauthorizedException(String msg) {
        super(msg);
    }
}