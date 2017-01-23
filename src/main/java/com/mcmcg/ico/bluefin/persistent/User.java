package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "roles", "legalEntities" })
@ToString(exclude = { "roles", "legalEntities" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "User_Lookup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "username")
public class User implements Serializable {
    private static final long serialVersionUID = 301195813236863721L;

    @PreUpdate
    @PrePersist
    public void beforePersist() {
        this.username = this.username.toLowerCase();
        if (StringUtils.isNotEmpty(this.email)) {
            this.email = this.email.toLowerCase();
        }
        this.firstName = StringUtils.capitalize(this.firstName);
        this.lastName = StringUtils.capitalize(this.lastName);
    }

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

    @Column(name = "IsActive")
    private Short isActive = 0;

    @Column(name = "Status")
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "LastLogin")
    private DateTime lastLogin;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserRole> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserLegalEntity> legalEntities;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "ModifiedBy")
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateUpdated")
    private DateTime dateUpdated = new DateTime();

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateModified", insertable = false, updatable = false)
    private DateTime modifiedDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

    public User() {
    }

    public User(final String username) {
        this.username = username;
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

    public void addLegalEntityApp(Long legalEntityAppId) {
        if (legalEntities == null) {
            legalEntities = new ArrayList<UserLegalEntity>();
        }

        UserLegalEntity userLE = new UserLegalEntity();
        userLE.setLegalEntityApp(new LegalEntityApp(legalEntityAppId));
        userLE.setUser(this);
        legalEntities.add(userLE);
    }
}
