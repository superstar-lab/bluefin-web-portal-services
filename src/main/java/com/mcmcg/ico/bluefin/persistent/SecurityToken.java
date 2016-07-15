package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Table(name = "SecurityToken_Backlist")
@Entity
public class SecurityToken implements Serializable {
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
}
