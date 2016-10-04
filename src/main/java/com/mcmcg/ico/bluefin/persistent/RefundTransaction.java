package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mcmcg.ico.bluefin.model.TransactionType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "saleTransaction" })
@ToString(exclude = { "saleTransaction" })
@Entity
@Table(name = "Refund_Transaction")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "refundTransactionId")
public class RefundTransaction implements Serializable, Transaction {
    private static final long serialVersionUID = 9099920942485693974L;

    @Id
    @Column(name = "RefundTransactionID")
    private Long refundTransactionId;

    @ManyToOne
    @JoinColumn(name = "SaleTransactionID")
    private SaleTransaction saleTransaction;

    // Transaction Detail
    @Transient
    private String transactionType = TransactionType.REFUND.toString();

    @Column(name = "OriginalSaleTransactionID")
    private String originalSaleTransactionID;

    @Column(name = "ApplicationTransactionID")
    private String applicationTransactionId;

    @Column(name = "ProcessorTransactionID")
    private String processorTransactionId;

    @Column(name = "MerchantID")
    private String merchantId;

    @Column(name = "RefundAmount")
    private BigDecimal refundAmount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "TransactionDateTime")
    private DateTime transactionDateTime;

    @Column(name = "ApprovalCode")
    private String approvalCode;

    @Column(name = "InternalResponseCode")
    private String internalResponseCode;

    @Column(name = "InternalResponseDescription")
    private String internalResponseDescription;

    @Column(name = "InternalStatusCode")
    private String internalStatusCode;

    @Column(name = "InternalStatusDescription")
    private String internalStatusDescription;

    @Column(name = "PaymentProcessorResponseCode")
    private String paymentProcessorResponseCode;

    @Column(name = "PaymentProcessorResponseCodeDescription")
    private String paymentProcessorResponseCodeDescription;

    @Column(name = "PaymentProcessorStatusCode")
    private String paymentProcessorStatusCode;

    @Column(name = "PaymentProcessorStatusCodeDescription")
    private String paymentProcessorStatusCodeDescription;

    // Misc
    @Column(name = "pUser")
    private String processUser;

    @Column(name = "Processor")
    private String processorName;

    @Column(name = "Application")
    private String application;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;
    
    // Reconciliation Status
    @Column(name = "ReconciliationStatusID")
    private Long reconciliationStatusID;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "ReconciliationDate", insertable = false, updatable = false)
    private DateTime reconciliationDate;
}
