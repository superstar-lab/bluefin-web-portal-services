package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "The request was invalid or cannot be otherwise served")
public class CustomBadRequestException extends RuntimeException {
    private static final long serialVersionUID = 4689755616500202839L;

    public CustomBadRequestException(String msg) {
        super(msg);
    }
}
