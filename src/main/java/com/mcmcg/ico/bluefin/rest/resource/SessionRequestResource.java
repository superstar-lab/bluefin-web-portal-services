package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

public class SessionRequestResource implements Serializable {

    private static final long serialVersionUID = 4390649295743233923L;

    @NotBlank(message = "Please provide a user name")
    private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
    
    
}
