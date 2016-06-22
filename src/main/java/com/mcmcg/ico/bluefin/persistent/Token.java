package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Table(name = "User_Login_Token")
@Entity
public class Token implements Serializable {
    private static final long serialVersionUID = 1181127309149973824L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TokenID")
    private long tokenId;
    @Column(name = "UserID")
    private long userId;
    @Column(name = "Token", columnDefinition = "TEXT")
    private String token;
    @Column(name = "Type")
    private String type;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "ExpireDate")
    private Date expire;
}
