package com.mcmcg.ico.bluefin.security.rest.resource;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class AuthenticationRequest {
    @NotEmpty(message = "username must not be empty")
    private String username;
    @NotEmpty(message = "password must not be empty")
    private String password;
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
    
    

}
