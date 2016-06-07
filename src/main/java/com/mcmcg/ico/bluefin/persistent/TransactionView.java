package com.mcmcg.ico.bluefin.persistent;

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
@Table(name = "Transaction_View")
public class TransactionView {

    private static final String CARD_MASK = "XXXX-XXXX-XXXX-";
    @Id
    @JsonIgnore
    @Column(name = "ID")
    private long id;
    @Column(name = "TransactionID")
    private String transactionId;
    @Column(name = "TransactionType")
    private String transactionType;
    @Column(name = "TransactionStatusCode")
    private Integer transactionStatusCode;
    @Column(name = "Customer")
    private String customer;
    @Column(name = "LegalEntity")
    private String legalEntity;
    @Column(name = "AccountNumber")
    private String accountNumber;
    @Column(name = "Amount", columnDefinition = "money")
    private BigDecimal amount;
    @Column(name = "CardType")
    private String cardType;
    @Column(name = "CardNumberLast4Char")
    private String cardNumberLast4Char;
    @Column(name = "ProcessorName")
    private String processorName;
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
