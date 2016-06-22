package com.mcmcg.ico.bluefin.rest.resource;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;

import lombok.Data;

@Data
public class UserResource {

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
    private List<Role> roles;
    @Size(min = 1, message = "legalEntityAppsList must not be empty")
    @NotNull(message = "legalEntityAppsList must not be null")
    private List<LegalEntityApp> legalEntityApps;

    public User toUser(List<UserRole> roles, List<UserLegalEntity> entities) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles(roles);
        user.setLegalEntities(entities);
        Date currentDate = new Date();
        user.setCreatedDate(currentDate);
        user.setDateUpdated(currentDate);
        user.setIsActive((short) 1);
        return user;
    }
}
