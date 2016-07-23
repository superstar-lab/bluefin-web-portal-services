package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import lombok.Data;

@Data
public class BasicTokenResponse implements Serializable {

    private static final long serialVersionUID = 4045833355784335235L;

    private String token;

    public BasicTokenResponse(String token) {
        this.token = token;
    }

}
