package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import lombok.Data;

@Data
public class UserResource {

    private String firstName;
    private String lastName;
    private String title;
    private String language;
    private String email;
    private List<String> roles;
    private List<String> legalEntityApps;

}
