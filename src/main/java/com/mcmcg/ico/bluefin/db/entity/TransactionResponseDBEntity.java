package com.mcmcg.ico.bluefin.db.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "transaction_response")
public class TransactionResponseDBEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int rowId;
    private int requestRowId;
    private String transactionId;
    private int statusCode;
    private String statusDescription;
    private String approvalCode;
    private String processorTransId;
    private String rspToken;
    private String processor;
    private BigDecimal amount;
    private String merchantId;
    private String responseCode;
    private String responseDesc;  
}
