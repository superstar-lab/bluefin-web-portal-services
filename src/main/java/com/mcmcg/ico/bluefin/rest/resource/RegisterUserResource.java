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

    @NotBlank(message = "username must not be empty")
    private String username;

    @NotBlank(message = "firstName must not be empty")
    @Pattern(regexp = "^[\\w]*$", message = "firstName must be alphanumeric")
    private String firstName;

    @NotBlank(message = "lastName must not be empty")
    @Pattern(regexp = "^[\\w]*$", message = "lastName must be alphanumeric")
    private String lastName;

    @NotBlank(message = "email must not be empty")
    private String email;

    @Size(min = 1, message = "rolesIdsList must not be empty")
    @NotNull(message = "roles must not be null")
    private Set<Long> roles;

    @Size(min = 1, message = "legalEntityApps must not be empty")
    @NotNull(message = "legalEntityApps must not be null")
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
