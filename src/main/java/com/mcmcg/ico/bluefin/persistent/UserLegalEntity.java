package com.mcmcg.ico.bluefin.persistent;

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
@Table(name = "User_LegalEntityApp")
public class UserLegalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "UserLegalEntityAppID")
    private long userLegalEntityAppId;

    @ManyToOne
    @JoinColumn(name = "LegalEntityAppID")
    private LegalEntityApp legalEntityApp;
    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    private Date createdDate;

}