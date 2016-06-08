package com.mcmcg.ico.bluefin.rest.resource;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;

import lombok.Data;

@Data
public class RegisterUserResource {

    @NotEmpty(message = "username must not be empty")
    private String username;
    @NotEmpty(message = "firstName must not be empty")
    private String firstName;
    @NotEmpty(message = "lastName must not be empty")
    private String lastName;
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
    
    public UserResource toUserResource(List<Role> roles, List<LegalEntityApp> entities) {
        UserResource userResource = new UserResource();
        userResource.setUsername(username);
        userResource.setFirstName(firstName);
        userResource.setLastName(lastName);
        userResource.setEmail(email);
        userResource.setRoles(roles);
        userResource.setLegalEntityApps(entities);
        return userResource;
    }
    
    public User toUser(List<UserRole> roles, List<UserLegalEntity> entities) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUserRoles(roles);
        user.setUserLegalEntities(entities);
        Date currentDate = new Date();
        user.setCreatedDate(currentDate);
        user.setDateUpdated(currentDate);
        user.setIsActive((short) 1);
        return user;
    }
}
