package com.mcmcg.ico.bluefin.security.rest.resource;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotEmpty(message = "username must not be empty")
    private String username;
    @NotEmpty(message = "password must not be empty")
    private String password;
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
