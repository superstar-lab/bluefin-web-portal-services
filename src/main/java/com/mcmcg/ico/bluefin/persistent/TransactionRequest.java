package com.mcmcg.ico.bluefin.persistent;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Entity
@Data
@Table(name = "transaction_request")
public class TransactionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int requestRowId;
    private String transactionId;
    private String firstName;
    private String lastName;
    private String processUser;
    private String actionType;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String cardNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date expiryDate;
    private String token;
    private BigDecimal chargeAmount;
    private String legalEntityApp;
    private String accountId;
    private Boolean testMode;
    private String merchantId;
    private String processor;
    private String application;
    private String cvv2Code;
    private String origin;
    private Date createdDate;

}
