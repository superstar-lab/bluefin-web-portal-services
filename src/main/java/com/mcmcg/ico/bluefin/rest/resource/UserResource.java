package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;

import lombok.Data;

@Data
public class UserResource implements Serializable {
    private static final long serialVersionUID = -1742184663149300007L;

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

    @Size(min = 1, message = "roleList must not be empty")
    @NotNull(message = "roleList must not be null")
    private Set<Role> roles;

    @Size(min = 1, message = "legalEntityAppsList must not be empty")
    @NotNull(message = "legalEntityAppsList must not be null")
    private Set<LegalEntityApp> legalEntityApps;

    public UserResource() {
    }

    public UserResource(User user) {
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.legalEntityApps = new HashSet<LegalEntityApp>(user.getLegalEntityApps());
        this.roles = new HashSet<Role>(user.getRoleNames());
    }

    public User toUser(Set<UserRole> roles, Set<UserLegalEntity> entities) {
        User user = new User();

        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles(roles);
        user.setLegalEntities(entities);

        return user;
    }
}
