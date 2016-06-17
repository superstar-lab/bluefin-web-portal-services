package com.mcmcg.ico.bluefin.persistent;

import java.math.BigDecimal;
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

@Entity
@Table(name = "Sale_Request")
@Data
public class SaleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SaleRequestID")
    private long saleRequestID;
    @Column(name = "TransactionType")
    private String transactionType;
    @Column(name = "FirstName")
    private String firstName;
    @Column(name = "LastName")
    private String lastName;
    @Column(name = "ProcessUser")
    private String processUser;
    @Column(name = "ActionType")
    private String actionType;
    @Column(name = "Address1")
    private String address1;
    @Column(name = "Address2")
    private String address2;
    @Column(name = "City")
    private String city;
    @Column(name = "State")
    private String state;
    @Column(name = "PostalCode")
    private String postalCode;
    @Column(name = "Country")
    private String country;
    @Column(name = "CardNumberLast4Char")
    private String cardNumberLast4Char;
    @Column(name = "CardType")
    private String cardType;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "ExpiryDate")
    private Date expiryDate;
    @Column(name = "Token")
    private String token;
    @Column(name = "ChargeAmount", columnDefinition = "money")
    private BigDecimal chargeAmount;
    @Column(name = "LegalEntityApp")
    private String legalEntityApp;
    @Column(name = "AccountId")
    private String accountId;
    @Column(name = "ApplicationTransactionID")
    private String transactionId;
    @Column(name = "RoutingKey")
    private String routingKey;
    @Column(name = "TestMode")
    private Short testMode;
    @Column(name = "Processor")
    private String processor;
    @Column(name = "Application")
    private String application;
    @Column(name = "Cvv2Code")
    private String cvv2Code;
    @Column(name = "Origin")
    private String origin;
    @Column(name = "ABA")
    private String aba;
    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;
    @Column(name = "DateCreated")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;

}
