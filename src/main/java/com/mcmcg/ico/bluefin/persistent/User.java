package com.mcmcg.ico.bluefin.persistent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;

import lombok.Data;

@Data
@Entity
@Table(name = "User_Lookup")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "UserID")
    private long userId;
    @Column(name = "FirstName")
    private String firstName;
    @Column(name = "LastName")
    private String lastName;
    @Column(name = "UserName")
    private String username;
    @Column(name = "Email")
    private String email;
    @Column(name = "IsActive")
    private Short isActive;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "LastLogin")
    private Date lastLogin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    private Date createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateUpdated")
    private Date dateUpdated;
    
    @OneToMany(mappedBy = "user")
    private Collection<UserLoginHistory> userLoginHistory;

    @OneToMany(mappedBy = "user")
    private Collection<UserRole> userRoles;

    @OneToMany(mappedBy = "user")
    private Collection<UserLegalEntity> userLegalEntities;

    public UserResource toUserResource() {
        UserResource userResource = new UserResource();
        userResource.setUsername(username);
        userResource.setFirstName(firstName);
        userResource.setLastName(lastName);
        userResource.setLegalEntityApps(getLegalEntityApps(userLegalEntities));
        userResource.setRoles(getRoleNames(userRoles));
        userResource.setEmail(email);
        return userResource;
    }

    private List<String> getRoleNames(Collection<UserRole> roleList) {
        List<String> roleNames = new ArrayList<String>();
        for (UserRole role : roleList) {
            roleNames.add(role.getRole().getRoleName());
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
