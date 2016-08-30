package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;

import lombok.Data;

@Data
public class RegisterUserResource implements Serializable {
    private static final long serialVersionUID = 3833640597296293196L;

    @NotBlank(message = "Please provide a user name")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Username must be alphanumeric")
    private String username;

    @NotBlank(message = "Please provide a first name for the user")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Firstname must be alphanumeric")
    private String firstName;

    @NotBlank(message = "Please provide a last name for the user")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Lastname must be alphanumeric")
    private String lastName;

    @NotBlank(message = "Please provide an email address for the user")
    private String email;

    @Size(min = 1, message = "Please provide a role for the user")
    @NotNull(message = "Please provide a role for the user")
    private Set<Long> roles;

    @Size(min = 1, message = "Please provide a legal entity for the user")
    @NotNull(message = "Please provide a legal entity for the user")
    private Set<Long> legalEntityApps;

    public User toUser(Collection<Role> roles, Collection<LegalEntityApp> legalEntityApps) {
        User user = new User();

        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        // Roles
        for (Role role : roles) {
            user.addRole(role);
        }
        // Legal Entity Apps
        for (LegalEntityApp legalEntityApp : legalEntityApps) {
            user.addLegalEntityApp(legalEntityApp);
        }

        return user;
    }
}
