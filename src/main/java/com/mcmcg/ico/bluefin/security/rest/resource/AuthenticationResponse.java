package com.mcmcg.ico.bluefin.security.rest.resource;

import java.util.List;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String token;
    private String username;
    private String firstName;
    private String lastName;
    private List<String> permissions;

}
