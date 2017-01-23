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

    @NotBlank(message = "Please provide a user name")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field user name must be alphanumeric")
    private String username;

    @NotBlank(message = "Please provide a first name for the user")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field first name must be alphanumeric")
    private String firstName;

    @NotBlank(message = "Please provide a last name for the user")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field last name must be alphanumeric")
    private String lastName;

    @NotBlank(message = "Please provide an email address for the user")
    private String email;

    @Size(min = 1, message = "Please provide a role for the user")
    @NotNull(message = "Please provide a role for the user")
    private Set<Role> roles;

    @Size(min = 1, message = "Please provide a legal entity for the user")
    @NotNull(message = "Please provide a legal entity for the user")
    private Set<LegalEntityApp> legalEntityApps;

    private String status;

    public UserResource() {
    }

    public UserResource(User user) {
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.status = user.getStatus();
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
        user.setStatus(status);
        user.setRoles(roles);
        user.setLegalEntities(entities);

        return user;
    }
}
