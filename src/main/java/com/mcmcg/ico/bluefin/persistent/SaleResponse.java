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

@Data
@Entity
@Table(name = "Sale_Response")
public class SaleResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SaleResponseID")
    private long saleResponseId;
    @Column(name = "SaleRequestID")
    private long saleRequestID;
    @Column(name = "StatusCode")
    private int statusCode;
    @Column(name = "StatusDescription")
    private String statusDescription;
    @Column(name = "ApprovalCode")
    private String approvalCode;
    @Column(name = "ProcessorTransactionID")
    private String processorTransactionId;
    @Column(name = "ApplicationTransactionID")
    private String applicationTransactionId;
    @Column(name = "rspToken")
    private String rspToken;
    @Column(name = "Amount", columnDefinition = "money")
    private BigDecimal amount;
    @Column(name = "RoutingKey")
    private String routingKey;
    @Column(name = "ResponseCode")
    private String responseCode;
    @Column(name = "ResponseDescription")
    private String responseDescription;
    @Column(name = "DateCreated")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;
}
