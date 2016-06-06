package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;

import lombok.Data;

@Data
public class UserResource {

    @NotEmpty(message = "username must not be empty")
    private String username;
    @NotEmpty(message = "firstName must not be empty")
    private String firstName;
    @NotEmpty(message = "lastName must not be empty")
    private String lastName;
    private String title;
    private String language;
    @NotEmpty(message = "email must not be empty")
    private String email;
    @Size(min = 1, message = "roleList must not be empty")
    @NotNull(message = "roleList must not be null")
    private List<String> roles;
    @Size(min = 1, message = "legalEntityAppsList must not be empty")
    @NotNull(message = "legalEntityAppsList must not be null")
    private List<String> legalEntityApps;
    @JsonIgnore
    private String password;

    public User toUser(List<Role> roles, List<UserLegalEntity> entities) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTitle(title);
        user.setLanguage(language);
        user.setEmail(email);
        user.setRoles(roles);
        user.setUserLegalEntities(entities);
        user.setPassword(password);
        return user;
    }
}
