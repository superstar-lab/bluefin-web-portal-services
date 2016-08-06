package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mcmcg.ico.bluefin.model.StatusCode;

import lombok.Data;

@SqlResultSetMapping(name = "CustomMappingResult", classes = {
        @ConstructorResult(targetClass = SaleTransaction.class, columns = {
                @ColumnResult(name = "SaleTransactionID", type = Long.class),
                @ColumnResult(name = "ApplicationTransactionID", type = String.class),
                @ColumnResult(name = "ProcessorTransactionID", type = String.class),
                @ColumnResult(name = "MerchantID", type = String.class),
                @ColumnResult(name = "TransactionType", type = String.class),
                @ColumnResult(name = "Processor", type = String.class),
                @ColumnResult(name = "StatusCode", type = Integer.class),
                @ColumnResult(name = "DateCreated", type = Date.class),
                @ColumnResult(name = "TransactionDateTime", type = Date.class),
                @ColumnResult(name = "ChargeAmount", type = BigDecimal.class),
                @ColumnResult(name = "FirstName", type = String.class),
                @ColumnResult(name = "LastName", type = String.class),
                @ColumnResult(name = "CardNumberLast4Char", type = String.class),
                @ColumnResult(name = "CardType", type = String.class),
                @ColumnResult(name = "LegalEntityApp", type = String.class),
                @ColumnResult(name = "AccountId", type = String.class) }) })
@Data
@Entity
@Table(name = "Sale_Transaction")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "saleTransactionId")
public class SaleTransaction implements Serializable, Transaction {
    private static final long serialVersionUID = 3783586860046594255L;
    private static final String CARD_MASK = "XXXX-XXXX-XXXX-";

    public SaleTransaction() {

    }

    public SaleTransaction(Long saleTransactionId, String applicationTransactionId, String processorTransactionId,
            String merchantID, String transactionType, String processorName, Integer transactionStatusCode,
            Date createdDate, Date transactionDateTime, BigDecimal amount, String firstName, String lastName,
            String cardNumberLast4Char, String cardType, String legalEntity, String accountNumber) {
        this.saleTransactionId = saleTransactionId;
        this.applicationTransactionId = applicationTransactionId;
        this.processorTransactionId = processorTransactionId;
        this.merchantId = merchantID;
        this.transactionType = transactionType;
        this.processorName = processorName;
        this.transactionStatusCode = transactionStatusCode;
        this.createdDate = createdDate;
        this.transactionDateTime = transactionDateTime;
        this.amount = amount;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cardNumberLast4Char = cardNumberLast4Char;
        this.cardType = cardType;
        this.legalEntity = legalEntity;
        this.accountNumber = accountNumber;
    }

    @Id
    @Column(name = "SaleTransactionID")
    private Long saleTransactionId;

    // Transaction Detail
    @Column(name = "TransactionType")
    private String transactionType;

    @Column(name = "LegalEntityApp")
    private String legalEntity;

    @Column(name = "AccountId")
    private String accountNumber;

    @Column(name = "ApplicationTransactionID")
    private String applicationTransactionId;

    @Column(name = "ProcessorTransactionID")
    private String processorTransactionId;

    @Column(name = "MerchantID")
    private String merchantId;

    @Column(name = "TransactionDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date transactionDateTime;

    // Credit Card Information
    @Column(name = "CardNumberFirst6Char")
    private String cardNumberFirst6Char;

    @Column(name = "CardNumberLast4Char")
    private String cardNumberLast4Char;

    @Column(name = "CardType")
    private String cardType;

    @Column(name = "ChargeAmount", columnDefinition = "money")
    private BigDecimal amount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/yy")
    @Column(name = "ExpiryDate")
    private Date expiryDate;

    // Billing Address
    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

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

    // Other
    @Column(name = "TestMode")
    private Short testMode;

    @Column(name = "Token")
    private String token;

    @Column(name = "Tokenized")
    private Short tokenized;

    @Column(name = "StatusCode")
    private Integer transactionStatusCode;

    @Column(name = "StatusDescription")
    private String statusDescription;

    @Column(name = "ProcessorResponseCode")
    private String processorResponseCode;

    @Column(name = "ProcessorResponseDescription")
    private String processorResponseDescription;

    @Column(name = "ApprovalCode")
    private String approvalCode;

    @Column(name = "InternalResponseCode")
    private String internalResponseCode;

    // Misc
    @Column(name = "ProcessUser")
    private String processUser;

    @Column(name = "Processor")
    private String processorName;

    @Column(name = "Application")
    private String application;

    @Column(name = "Origin")
    private String origin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    @JsonIgnore
    @OneToMany(mappedBy = "saleTransaction", fetch = FetchType.LAZY)
    private Collection<RefundTransaction> refundedTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "saleTransaction", fetch = FetchType.LAZY)
    private Collection<VoidTransaction> voidedTransactions;

    public StatusCode getTransactionStatusCode() {
        return StatusCode.valueOf(transactionStatusCode);
    }

    public String getCardNumberLast4Char() {
        return CARD_MASK + cardNumberLast4Char;
    }
}
