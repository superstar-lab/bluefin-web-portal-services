package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class BasicToken implements Serializable {

    private static final long serialVersionUID = 4045833355784335235L;

    @NotEmpty(message = "token must not be empty")
    private String token;

    public BasicToken(String token) {
        this.token = token;
    }

}
