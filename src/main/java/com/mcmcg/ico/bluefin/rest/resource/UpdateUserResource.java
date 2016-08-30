package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Set;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;

import lombok.Data;

@Data
public class UpdateUserResource implements Serializable {
    private static final long serialVersionUID = 5100870250264827468L;

    @NotBlank(message = "Please provide a first name for the user")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "firstName must be alphanumeric")
    private String firstName;

    @NotBlank(message = "Please provide a last name for the user")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "lastName must be alphanumeric")
    private String lastName;

    @NotBlank(message = "Please provide an email address for the user")
    private String email;

    public UserResource toUserResource(Set<Role> roles, Set<LegalEntityApp> entities) {
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
        return user;
    }

}
