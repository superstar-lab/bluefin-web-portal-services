package com.mcmcg.ico.bluefin.persistent;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmcg.ico.bluefin.rest.resource.StatusCode;

import lombok.Data;

@Data
@Entity
public class TransactionView {
    @Id
    @JsonIgnore
    private long id;
    private String transactionId;
    private String transactionType;
    private Integer transactionStatusCode;
    private String customer;
    private String legalEntity;
    private String accountNumber;
    private BigDecimal amount;
    private String cardType;
    @Column(name = "card_number_last_4_char")
    private String cardNumberLast4Char;
    private String processorName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;

    public StatusCode getTransactionStatusCode() {
        return StatusCode.valueOf(transactionStatusCode);
    }
}
