package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "Role_Lookup")
public class Role implements Serializable {
    private static final long serialVersionUID = -2465130966357082906L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "RoleID")
    private Long roleId;

    @Column(name = "RoleName")
    private String roleName;

    @Column(name = "Description")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Collection<RolePermission> rolePermissions;

    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Collection<UserRole> userRoles;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    private Date dateCreated;

    public Role() {
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }
}
