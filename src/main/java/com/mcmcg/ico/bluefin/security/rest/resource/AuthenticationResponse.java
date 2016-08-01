package com.mcmcg.ico.bluefin.security.rest.resource;

import java.io.Serializable;
import java.util.Set;

import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.persistent.Role;

import lombok.Data;

@Data
public class AuthenticationResponse implements Serializable {
    private static final long serialVersionUID = 3553426608016922894L;

    private String token;
    private String username;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
    private Set<Permission> permissions;
}
