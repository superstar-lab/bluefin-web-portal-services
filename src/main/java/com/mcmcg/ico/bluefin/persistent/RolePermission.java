package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "Role_Permission")
public class RolePermission implements Serializable {
    private static final long serialVersionUID = -3382648236142523952L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "RolePermissionID")
    private long rolePermissionId;

    @ManyToOne
    @JoinColumn(name = "RoleID")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "PermissionID")
    private Permission permission;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

}
