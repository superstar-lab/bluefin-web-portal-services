package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -3816342833505594895L;

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
