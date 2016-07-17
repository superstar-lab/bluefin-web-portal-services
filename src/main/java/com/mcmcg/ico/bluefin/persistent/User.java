package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;

import lombok.Data;

@Data
@Entity
@Table(name = "User_Lookup")
public class User implements Serializable {
    private static final long serialVersionUID = 301195813236863721L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "UserID")
    @JsonIgnore
    private long userId;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "UserName")
    private String username;

    @Column(name = "Email")
    private String email;

    @JsonIgnore
    @Column(name = "userPassword")
    private String userPassword;

    @Column(name = "IsActive")
    @JsonIgnore
    private Short isActive;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "LastLogin")
    @JsonIgnore
    private Date lastLogin;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<UserRole> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<UserLegalEntity> legalEntities;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    @JsonIgnore
    private Date createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateUpdated")
    @JsonIgnore
    private Date dateUpdated;

    public UserResource toUserResource() {
        UserResource userResource = new UserResource();
        userResource.setUsername(username);
        userResource.setFirstName(firstName);
        userResource.setLastName(lastName);
        userResource.setLegalEntityApps(getLegalEntityApps());
        userResource.setRoles(getRoleNames());
        userResource.setEmail(email);
        return userResource;
    }

    @JsonProperty("roles")
    public List<Role> getRoleNames() {
        return roles.stream().map(role -> role.getRole()).collect(Collectors.toList());
    }

    @JsonProperty("legalEntityApps")
    public List<LegalEntityApp> getLegalEntityApps() {
        return legalEntities.stream().map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityApp())
                .collect(Collectors.toList());
    }
}
