package com.mcmcg.ico.bluefin.persistent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.mcmcg.ico.bluefin.rest.resource.UserResource;

import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer userId;

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String language;
    private String title;

    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Collection<Role> roles;

    @OneToMany(mappedBy = "user")
    private Collection<UserLegalEntity> userLegalEntities;

    public UserResource toUserResource() {
        UserResource userResource = new UserResource();
        userResource.setEmail(email);
        userResource.setFirstName(firstName);
        userResource.setLanguage(language);
        userResource.setLastName(lastName);
        userResource.setLegalEntityApps(getLegalEntityApps(userLegalEntities));
        userResource.setRoles(getRoleNames(roles));
        userResource.setTitle(title);
        return userResource;
    }

    private List<String> getRoleNames(Collection<Role> roleList) {
        List<String> roleNames = new ArrayList<String>();
        for (Role role : roleList) {
            roleNames.add(role.getRoleName());
        }
        return roleNames;
    }

    private List<String> getLegalEntityApps(Collection<UserLegalEntity> userLegalEntityAppList) {
        List<String> legalEntityApps = new ArrayList<String>();
        for (UserLegalEntity userLegalEntityApp : userLegalEntityAppList) {
            legalEntityApps.add(userLegalEntityApp.getLegalEntityApp().getLegalEntityAppName());
        }
        return legalEntityApps;
    }
}
