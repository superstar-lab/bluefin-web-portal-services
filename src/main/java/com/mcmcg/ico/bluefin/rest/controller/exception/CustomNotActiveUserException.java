package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "User account not activated yet.")
public class CustomNotActiveUserException extends RuntimeException {

    private static final long serialVersionUID = -2125288439743361226L;

    public CustomNotActiveUserException(String msg) {
        super(msg);
    }
}
