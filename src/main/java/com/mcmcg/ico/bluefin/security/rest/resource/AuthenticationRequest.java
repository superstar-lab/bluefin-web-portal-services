package com.mcmcg.ico.bluefin.security.rest.resource;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import lombok.Data;

@Data
public class AuthenticationRequest {

    private String username;
    private String password;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
