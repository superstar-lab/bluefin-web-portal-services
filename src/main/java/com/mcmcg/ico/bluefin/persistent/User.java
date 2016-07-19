package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.ArrayList;
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

import lombok.Data;

@Data
@Entity
@Table(name = "User_Lookup")
public class User implements Serializable {
    private static final long serialVersionUID = 301195813236863721L;

    @JsonIgnore
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

    @JsonIgnore
    @Column(name = "userPassword")
    private String userPassword;

    @JsonIgnore
    @Column(name = "IsActive")
    private Short isActive = (short) 0;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "LastLogin")
    private Date lastLogin;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<UserRole> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<UserLegalEntity> legalEntities;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateUpdated")
    private Date dateUpdated;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    @JsonProperty("roles")
    public List<Role> getRoleNames() {
        return roles.stream().map(role -> role.getRole()).collect(Collectors.toList());
    }

    @JsonProperty("legalEntityApps")
    public List<LegalEntityApp> getLegalEntityApps() {
        return legalEntities.stream().map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityApp())
                .collect(Collectors.toList());
    }

    public void addRole(Role role) {
        if (roles == null) {
            roles = new ArrayList<UserRole>();
        }

        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(this);
        roles.add(userRole);
    }

    public void addLegalEntityApp(LegalEntityApp legalEntityApp) {
        if (legalEntities == null) {
            legalEntities = new ArrayList<UserLegalEntity>();
        }

        UserLegalEntity userLE = new UserLegalEntity();
        userLE.setLegalEntityApp(legalEntityApp);
        userLE.setUser(this);
        legalEntities.add(userLE);
    }
}
