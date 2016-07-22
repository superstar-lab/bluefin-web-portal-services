package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "User_Role")
public class UserRole implements Serializable {
    private static final long serialVersionUID = 8423689499232150573L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "UserRoleID")
    private Long userRoleId;

    @ManyToOne
    @JoinColumn(name = "RoleID")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    public UserRole() {
    }

    public UserRole(Long value) {
        this.userRoleId = value;
    }
}