package com.mcmcg.ico.bluefin.rest.resource;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;

import lombok.Data;

@Data
public class UpdateUserResource {

    @NotEmpty(message = "firstName must not be empty")
    private String firstName;
    @NotEmpty(message = "lastName must not be empty")
    private String lastName;
    @NotEmpty(message = "email must not be empty")
    private String email;

    public UserResource toUserResource(List<Role> roles, List<LegalEntityApp> entities) {
        UserResource userResource = new UserResource();
        userResource.setFirstName(firstName);
        userResource.setLastName(lastName);
        userResource.setEmail(email);
        userResource.setRoles(roles);
        userResource.setLegalEntityApps(entities);
        return userResource;
    }

    public User updateUser(User user) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setDateUpdated(new Date());
        return user;
    }

}