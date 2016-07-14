package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmcg.ico.bluefin.rest.resource.StatusCode;

import lombok.Data;

@Data
@Entity
@Table(name = "Sale_Transaction")
public class SaleTransaction implements Serializable {
    private static final long serialVersionUID = 3783586860046594255L;

    private static final String CARD_MASK = "XXXX-XXXX-XXXX-";
    @Id
    @JsonIgnore
    @Column(name = "SaleTransactionID")
    private Long saleTransactionId;
    @Column(name = "FirstName")
    private String firstName;
    @Column(name = "LastName")
    private String lastName;
    @JsonIgnore
    @Column(name = "ProcessUser")
    private String processUser;
    @Column(name = "TransactionType")
    private String transactionType;
    @JsonIgnore
    @Column(name = "Address1")
    private String address1;
    @JsonIgnore
    @Column(name = "Address2")
    private String address2;
    @JsonIgnore
    @Column(name = "City")
    private String city;
    @JsonIgnore
    @Column(name = "State")
    private String state;
    @JsonIgnore
    @Column(name = "PostalCode")
    private String postalCode;
    @JsonIgnore
    @Column(name = "Country")
    private String country;
    @JsonIgnore
    @Column(name = "CardNumberFirst6Char")
    private String cardNumberFirst6Char;
    @Column(name = "CardNumberLast4Char")
    private String cardNumberLast4Char;
    @Column(name = "CardType")
    private String cardType;
    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "ExpiryDate")
    private Date expiryDate;
    @JsonIgnore
    @Column(name = "Token")
    private String token;
    @Column(name = "LegalEntityApp")
    private String legalEntity;
    @Column(name = "AccountId")
    private String accountNumber;
    @Column(name = "ApplicationTransactionID")
    private String transactionId;
    @JsonIgnore
    @Column(name = "MerchantID")
    private String merchantId;
    @Column(name = "Processor")
    private String processorName;
    @JsonIgnore
    @Column(name = "Application")
    private String application;
    @JsonIgnore
    @Column(name = "Origin")
    private String origin;
    @JsonIgnore
    @Column(name = "TransactionDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date transactionDateTime;
    @JsonIgnore
    @Column(name = "TestMode")
    private Short testMode;
    @Column(name = "StatusCode")
    private Integer transactionStatusCode;
    @JsonIgnore
    @Column(name = "StatusDescription")
    private String statusDescription;
    @JsonIgnore
    @Column(name = "ApprovalCode")
    private String approvalCode;
    @Column(name = "ChargeAmount", columnDefinition = "money")
    private BigDecimal amount;
    @JsonIgnore
    @Column(name = "ResponseCode")
    private String responseCode;
    @JsonIgnore
    @Column(name = "ResponseDescription")
    private String responseDescription;
    @JsonIgnore
    @Column(name = "ProcessorTransactionID")
    private String processorTransactionId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    private Date createdDate;

    public StatusCode getTransactionStatusCode() {
        return StatusCode.valueOf(transactionStatusCode);
    }

    public String getCardNumberLast4Char() {
        return CARD_MASK + cardNumberLast4Char;
    }
}
