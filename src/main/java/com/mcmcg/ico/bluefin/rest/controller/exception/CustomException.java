package com.mcmcg.ico.bluefin.rest.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Something is broken")
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = -6302949695810242481L;

    public CustomException(String msg) {
        super(msg);
    }
}
