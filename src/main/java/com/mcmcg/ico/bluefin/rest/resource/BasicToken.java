package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

public class BasicToken implements Serializable {

    private static final long serialVersionUID = 4045833355784335235L;

    @NotEmpty(message = "token must not be empty")
    private String token;

    public BasicToken(String token) {
        this.token = token;
    }

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

    
}
