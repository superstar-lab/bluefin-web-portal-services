package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import lombok.Data;

@Data
public class AccountResource {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private List<String> roles;
}
