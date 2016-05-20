package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You don't have permission to access resource")
public class CustomForbiddenException extends RuntimeException {
    private static final long serialVersionUID = -4523493904519085994L;

    public CustomForbiddenException(String msg) {
        super(msg);
    }
}
