package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mcmcg.ico.bluefin.model.PaymentFrequency;
import com.mcmcg.ico.bluefin.rest.resource.Views;

import lombok.Data;

@SqlResultSetMapping(name = "PaymentProcessorRemittanceCustomMappingResult", classes = {
        @ConstructorResult(targetClass = PaymentProcessorRemittance.class, columns = {
                @ColumnResult(name = "PaymentProcessorRemittanceID", type = Long.class),
                @ColumnResult(name = "DateCreated", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "ReconciliationStatusID", type = Long.class),
                @ColumnResult(name = "ReconciliationDate", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "PaymentMethod", type = String.class),
                @ColumnResult(name = "TransactionAmount", type = BigDecimal.class),
                @ColumnResult(name = "TransactionType", type = String.class),
                @ColumnResult(name = "TransactionTime", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "AccountId", type = String.class),
                @ColumnResult(name = "Application", type = String.class),
                @ColumnResult(name = "ProcessorTransactionID", type = String.class),
                @ColumnResult(name = "MerchantID", type = String.class),
                @ColumnResult(name = "TransactionSource", type = String.class),
                @ColumnResult(name = "FirstName", type = String.class),
                @ColumnResult(name = "LastName", type = String.class),
                @ColumnResult(name = "RemittanceCreationDate", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "PaymentProcessorID", type = Long.class),
                @ColumnResult(name = "ProcessorName", type = String.class),
                @ColumnResult(name = "SaleTransactionID", type = Long.class),
                @ColumnResult(name = "SaleTransactionType", type = String.class),
                @ColumnResult(name = "SaleLegalEntityApp", type = String.class),
                @ColumnResult(name = "SaleAccountId", type = String.class),
                @ColumnResult(name = "SaleApplicationTransactionID", type = String.class),
                @ColumnResult(name = "SaleProcessorTransactionID", type = String.class),
                @ColumnResult(name = "SaleMerchantID", type = String.class),
                @ColumnResult(name = "SaleTransactionDateTime", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "SaleCardNumberFirst6Char", type = String.class),
                @ColumnResult(name = "SaleCardNumberLast4Char", type = String.class),
                @ColumnResult(name = "SaleCardType", type = String.class),
                @ColumnResult(name = "SaleChargeAmount", type = BigDecimal.class),
                @ColumnResult(name = "SaleExpiryDate", type = Date.class),
                @ColumnResult(name = "SaleFirstName", type = String.class),
                @ColumnResult(name = "SaleLastName", type = String.class),
                @ColumnResult(name = "SaleAddress1", type = String.class),
                @ColumnResult(name = "SaleAddress2", type = String.class),
                @ColumnResult(name = "SaleCity", type = String.class),
                @ColumnResult(name = "SaleState", type = String.class),
                @ColumnResult(name = "SalePostalCode", type = String.class),
                @ColumnResult(name = "SaleCountry", type = String.class),
                @ColumnResult(name = "SaleTestMode", type = Short.class),
                @ColumnResult(name = "SaleToken", type = String.class),
                @ColumnResult(name = "SaleTokenized", type = Short.class),
                @ColumnResult(name = "SalePaymentProcessorResponseCode", type = String.class),
                @ColumnResult(name = "SalePaymentProcessorResponseCodeDescription", type = String.class),
                @ColumnResult(name = "SaleApprovalCode", type = String.class),
                @ColumnResult(name = "SaleInternalResponseCode", type = String.class),
                @ColumnResult(name = "SaleInternalResponseDescription", type = String.class),
                @ColumnResult(name = "SaleInternalStatusCode", type = String.class),
                @ColumnResult(name = "SaleInternalStatusDescription", type = String.class),
                @ColumnResult(name = "SalePaymentProcessorStatusCode", type = String.class),
                @ColumnResult(name = "SalePaymentProcessorStatusCodeDescription", type = String.class),
                @ColumnResult(name = "SalePaymentProcessorRuleID", type = Long.class),
                @ColumnResult(name = "SaleRulePaymentProcessorID", type = Long.class),
                @ColumnResult(name = "SaleRuleCardType", type = String.class),
                @ColumnResult(name = "SaleRuleMaximumMonthlyAmount", type = BigDecimal.class),
                @ColumnResult(name = "SaleRuleNoMaximumMonthlyAmountFlag", type = Short.class),
                @ColumnResult(name = "SaleRulePriority", type = Short.class),
                @ColumnResult(name = "SaleProcessUser", type = String.class),
                @ColumnResult(name = "SaleProcessor", type = String.class),
                @ColumnResult(name = "SaleApplication", type = String.class),
                @ColumnResult(name = "SaleOrigin", type = String.class),
                @ColumnResult(name = "SaleAccountPeriod", type = String.class),
                @ColumnResult(name = "SaleDesk", type = String.class),
                @ColumnResult(name = "SaleInvoiceNumber", type = String.class),
                @ColumnResult(name = "SaleUserDefinedField1", type = String.class),
                @ColumnResult(name = "SaleUserDefinedField2", type = String.class),
                @ColumnResult(name = "SaleUserDefinedField3", type = String.class),
                @ColumnResult(name = "SaleDateCreated", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "SaleIsVoided", type = Integer.class),
                @ColumnResult(name = "SaleIsRefunded", type = Integer.class),
                @ColumnResult(name = "SalePaymentProcessorInternalStatusCodeID", type = Long.class),
                @ColumnResult(name = "SalePaymentProcessorInternalResponseCodeID", type = Long.class),
                @ColumnResult(name = "SaleReconciliationStatusID", type = Long.class),
                @ColumnResult(name = "SaleReconciliationDate", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "SaleBatchUploadID", type = Long.class),
                @ColumnResult(name = "Processor_Name", type = String.class),
                @ColumnResult(name = "MID", type = String.class),
                @ColumnResult(name = "ReconciliationStatus_ID", type = String.class) }) })
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PaymentProcessor_Remittance")
public class PaymentProcessorRemittance implements Serializable, Transaction {

    private static final long serialVersionUID = -1312687866731930904L;

    public PaymentProcessorRemittance() {
    }

    public PaymentProcessorRemittance(Long paymentProcessorRemittanceId, DateTime createdDate,
            Long reconciliationStatusId, DateTime reconciliationDate, String paymentMethod,
            BigDecimal transactionAmount, String transactionType, DateTime transactionTime, String accountId,
            String application, String processorTransactionId, String merchantId, String transactionSource,
            String firstName, String lastName, DateTime remittanceCreationDate, Long paymentProcessorId,
            String processorName, Long saleTransactionId, String saleTransactionType, String saleLegalEntityApp,
            String saleAccountNumber, String saleApplicationTransactionId, String saleProcessorTransactionId,
            String saleMerchantId, DateTime saleTransactionDateTime, String saleCardNumberFirst6Char,
            String saleCardNumberLast4Char, String saleCardType, BigDecimal saleAmount, Date saleExpiryDate,
            String saleFirstName, String saleLastName, String saleAddress1, String saleAddress2, String saleCity,
            String saleState, String salePostalCode, String saleCountry, Short saleTestMode, String saleToken,
            Short saleTokenized, String salePaymentProcessorResponseCode,
            String salePaymentProcessorResponseCodeDescription, String saleApprovalCode,
            String saleInternalResponseCode, String saleInternalResponseDescription, String saleInternalStatusCode,
            String saleInternalStatusDescription, String salePaymentProcessorStatusCode,
            String salePaymentProcessorStatusCodeDescription, Long salePaymentProcessorRuleId,
            Long saleRulePaymentProcessorId, String saleRuleCardType, BigDecimal saleRuleMaximumMonthlyAmount,
            Short saleRuleNoMaximumMonthlyAmountFlag, Short saleRulePriority, String saleProcessUser,
            String saleProcessorName, String saleApplication, String saleOrigin, String saleAccountPeriod,
            String saleDesk, String saleInvoiceNumber, String saleUserDefinedField1, String saleUserDefinedField2,
            String saleUserDefinedField3, DateTime saleCreatedDate, Integer saleIsVoided, Integer saleIsRefunded,
            Long salePaymentProcessorInternalStatusCodeId, Long salePaymentProcessorInternalResponseCodeId,
            Long saleReconciliationStatusId, DateTime saleReconciliationDate, Long saleBatchUploadId,
            String Processor_Name, String MID, String ReconciliationStatus_ID) {
        this.paymentProcessorRemittanceId = paymentProcessorRemittanceId;
        this.createdDate = createdDate;
        this.reconciliationStatusId = reconciliationStatusId;
        this.reconciliationDate = reconciliationDate;
        this.paymentMethod = paymentMethod;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
        this.transactionTime = transactionTime;
        this.accountId = accountId;
        this.application = application;
        this.processorTransactionId = processorTransactionId;
        this.merchantId = merchantId;
        this.transactionSource = transactionSource;
        this.firstName = firstName;
        this.lastName = lastName;
        this.remittanceCreationDate = remittanceCreationDate;
        this.paymentProcessorId = paymentProcessorId;
        this.processorName = processorName;
        this.saleTransactionId = saleTransactionId;
        this.saleTransactionType = saleTransactionType;
        this.saleLegalEntityApp = saleLegalEntityApp;
        this.saleAccountNumber = saleAccountNumber;
        this.saleApplicationTransactionId = saleApplicationTransactionId;
        this.saleProcessorTransactionId = saleProcessorTransactionId;
        this.saleMerchantId = saleMerchantId;
        this.saleTransactionDateTime = saleTransactionDateTime;
        this.saleCardNumberFirst6Char = saleCardNumberFirst6Char;
        this.saleCardNumberLast4Char = saleCardNumberLast4Char;
        this.saleCardType = saleCardType;
        this.saleAmount = saleAmount;
        this.saleExpiryDate = saleExpiryDate;
        this.saleFirstName = saleFirstName;
        this.saleLastName = saleLastName;
        this.saleAddress1 = saleAddress1;
        this.saleAddress2 = saleAddress2;
        this.saleCity = saleCity;
        this.saleState = saleState;
        this.salePostalCode = salePostalCode;
        this.saleCountry = saleCountry;
        this.saleTestMode = saleTestMode;
        this.saleToken = saleToken;
        this.saleTokenized = saleTokenized;
        this.salePaymentProcessorResponseCode = salePaymentProcessorResponseCode;
        this.salePaymentProcessorResponseCodeDescription = salePaymentProcessorResponseCodeDescription;
        this.saleApprovalCode = saleApprovalCode;
        this.saleInternalResponseCode = saleInternalResponseCode;
        this.saleInternalResponseDescription = saleInternalResponseDescription;
        this.saleInternalStatusCode = saleInternalStatusCode;
        this.saleInternalStatusDescription = saleInternalStatusDescription;
        this.salePaymentProcessorStatusCode = salePaymentProcessorStatusCode;
        this.salePaymentProcessorStatusCodeDescription = salePaymentProcessorStatusCodeDescription;
        this.salePaymentProcessorRuleId = salePaymentProcessorRuleId;
        this.saleRulePaymentProcessorId = saleRulePaymentProcessorId;
        this.saleRuleCardType = saleRuleCardType;
        this.saleRuleMaximumMonthlyAmount = saleRuleMaximumMonthlyAmount;
        this.saleRuleNoMaximumMonthlyAmountFlag = saleRuleNoMaximumMonthlyAmountFlag;
        this.saleRulePriority = saleRulePriority;
        this.saleProcessUser = saleProcessUser;
        this.saleProcessorName = saleProcessorName;
        this.saleApplication = saleApplication;
        this.saleOrigin = saleOrigin;
        this.saleAccountPeriod = saleAccountPeriod;
        this.saleDesk = saleDesk;
        this.saleInvoiceNumber = saleInvoiceNumber;
        this.saleUserDefinedField1 = saleUserDefinedField1;
        this.saleUserDefinedField2 = saleUserDefinedField2;
        this.saleUserDefinedField3 = saleUserDefinedField3;
        this.saleCreatedDate = saleCreatedDate;
        this.saleIsVoided = saleIsVoided;
        this.saleIsRefunded = saleIsRefunded;
        this.salePaymentProcessorInternalStatusCodeId = salePaymentProcessorInternalStatusCodeId;
        this.salePaymentProcessorInternalResponseCodeId = salePaymentProcessorInternalResponseCodeId;
        this.saleReconciliationStatusId = saleReconciliationStatusId;
        this.saleReconciliationDate = saleReconciliationDate;
        this.saleBatchUploadId = saleBatchUploadId;
        this.Processor_Name = Processor_Name;
        this.MID = MID;
        this.ReconciliationStatus_ID = ReconciliationStatus_ID;
    }

    @JsonProperty("remittance.paymentProcessorRemittanceId")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorRemittanceID")
    private Long paymentProcessorRemittanceId;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

    @JsonProperty("remittance.reconciliationStatusId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    @Column(name = "ReconciliationStatusID")
    private Long reconciliationStatusId;

    @JsonProperty("remittance.reconciliationDate")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "ReconciliationDate")
    private DateTime reconciliationDate;

    @JsonProperty("remittance.paymentMethod")
    @Column(name = "PaymentMethod")
    private String paymentMethod;

    @JsonProperty("remittance.transactionAmount")
    @Column(name = "TransactionAmount", columnDefinition = "money")
    private BigDecimal transactionAmount;

    @JsonProperty("remittance.transactionType")
    @Column(name = "TransactionType")
    private String transactionType;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "TransactionTime", insertable = false, updatable = false)
    private DateTime transactionTime;

    @JsonProperty("remittance.accountId")
    @Column(name = "AccountID")
    private String accountId;

    @JsonProperty("remittance.application")
    @Column(name = "Application")
    private String application;

    @JsonProperty("remittance.processorTransactionId")
    @Column(name = "ProcessorTransactionID")
    private String processorTransactionId;

    @JsonProperty("remittance.merchantId")
    @Column(name = "MerchantID")
    private String merchantId;

    @JsonProperty("remittance.transactionSource")
    @Column(name = "TransactionSource")
    private String transactionSource;

    @JsonProperty("remittance.firstName")
    @Column(name = "FirstName")
    private String firstName;

    @JsonProperty("remittance.lastName")
    @Column(name = "LastName")
    private String lastName;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "RemittanceCreationDate", insertable = false, updatable = false)
    private DateTime remittanceCreationDate;

    @JsonProperty("remittance.paymentProcessorId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    @Column(name = "PaymentProcessorID")
    private Long paymentProcessorId;

    @Transient
    @JsonIgnore
    private String transactionId;

    @Transient
    @JsonIgnore
    private String legalEntity;

    @Transient
    @JsonIgnore
    private String processorName;

    @JsonProperty("remittance.processorName")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String getProcessorName() {
        return processorName;
    }

    @Transient
    @JsonProperty("sale.saleTransactionId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long saleTransactionId;

    @Transient
    @JsonProperty("sale.transactionType")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleTransactionType;

    @Transient
    @JsonProperty("sale.legalEntityApp")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleLegalEntityApp;

    @Transient
    @JsonProperty("sale.accountNumber")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleAccountNumber;

    @Transient
    @JsonProperty("sale.applicationTransactionId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleApplicationTransactionId;

    @Transient
    @JsonProperty("sale.processorTransactionId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleProcessorTransactionId;

    @Transient
    @JsonProperty("sale.merchantId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleMerchantId;

    @Transient
    @JsonProperty("sale.transactionDateTime")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private DateTime saleTransactionDateTime;

    @Transient
    @JsonProperty("sale.cardNumberFirst6Char")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleCardNumberFirst6Char;

    @Transient
    @JsonProperty("sale.cardNumberLast4Char")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleCardNumberLast4Char;

    @Transient
    @JsonProperty("sale.cardType")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleCardType;

    @Transient
    @JsonProperty("sale.amount")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private BigDecimal saleAmount;

    @Transient
    @JsonProperty("sale.expiryDate")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Date saleExpiryDate;

    @Transient
    @JsonProperty("sale.firstName")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleFirstName;

    @Transient
    @JsonProperty("sale.lastName")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleLastName;

    @Transient
    @JsonProperty("sale.address1")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleAddress1;

    @Transient
    @JsonProperty("sale.address2")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleAddress2;

    @Transient
    @JsonProperty("sale.city")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleCity;

    @Transient
    @JsonProperty("sale.state")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleState;

    @Transient
    @JsonProperty("sale.postalCode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String salePostalCode;

    @Transient
    @JsonProperty("sale.country")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleCountry;

    @Transient
    @JsonProperty("sale.testMode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Short saleTestMode;

    @Transient
    @JsonProperty("sale.token")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleToken;

    @Transient
    @JsonProperty("sale.tokenized")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Short saleTokenized;

    @Transient
    @JsonProperty("sale.paymentProcessorResponseCode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String salePaymentProcessorResponseCode;

    @Transient
    @JsonProperty("sale.paymentProcessorResponseCodeDescription")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String salePaymentProcessorResponseCodeDescription;

    @Transient
    @JsonProperty("sale.approvalCode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleApprovalCode;

    @Transient
    @JsonProperty("sale.internalResponseCode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleInternalResponseCode;

    @Transient
    @JsonProperty("sale.internalResponseDescription")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleInternalResponseDescription;

    @Transient
    @JsonProperty("sale.internalStatusCode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleInternalStatusCode;

    @Transient
    @JsonProperty("sale.internalStatusDescription")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleInternalStatusDescription;

    @Transient
    @JsonProperty("sale.paymentProcessorStatusCode")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String salePaymentProcessorStatusCode;

    @Transient
    @JsonProperty("sale.paymentProcessorStatusCodeDescription")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String salePaymentProcessorStatusCodeDescription;

    @Transient
    @JsonProperty("sale.paymentProcessorInternalStatusCodeId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long salePaymentProcessorInternalStatusCodeId;

    @Transient
    @JsonProperty("sale.paymentProcessorInternalResponseCodeId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long salePaymentProcessorInternalResponseCodeId;

    @Transient
    @JsonProperty("sale.paymentProcessorRuleId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long salePaymentProcessorRuleId;

    @Transient
    @JsonProperty("sale.rulePaymentProcessorId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long saleRulePaymentProcessorId;

    @Transient
    @JsonProperty("sale.ruleCardType")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleRuleCardType;

    @Transient
    @JsonProperty("sale.ruleMaximumMonthlyAmount")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private BigDecimal saleRuleMaximumMonthlyAmount;

    @Transient
    @JsonProperty("sale.ruleNoMaximumMonthlyAmountFlag")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Short saleRuleNoMaximumMonthlyAmountFlag;

    @Transient
    @JsonProperty("sale.rulePriority")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Short saleRulePriority;

    @Transient
    @JsonProperty("sale.processUser")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleProcessUser;

    @Transient
    @JsonProperty("sale.processorName")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleProcessorName;

    @Transient
    @JsonProperty("sale.application")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleApplication;

    @Transient
    @JsonProperty("sale.origin")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleOrigin;

    @Transient
    @JsonProperty("sale.accountPeriod")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleAccountPeriod;

    @Transient
    @JsonProperty("sale.desk")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleDesk;

    @Transient
    @JsonProperty("sale.invoiceNumber")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleInvoiceNumber;

    @Transient
    @JsonProperty("sale.userDefinedField1")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleUserDefinedField1;

    @Transient
    @JsonProperty("sale.userDefinedField2")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleUserDefinedField2;

    @Transient
    @JsonProperty("sale.userDefinedField3")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String saleUserDefinedField3;

    @Transient
    @JsonProperty("sale.reconciliationStatusId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long saleReconciliationStatusId;

    @Transient
    @JsonProperty("sale.reconciliationDate")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private DateTime saleReconciliationDate;

    @Transient
    @JsonProperty("sale.batchUploadId")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Long saleBatchUploadId;

    @Transient
    @JsonProperty("sale.createdDate")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private DateTime saleCreatedDate;

    @Transient
    @JsonProperty("sale.isVoided")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Integer saleIsVoided;

    @Transient
    @JsonProperty("sale.isRefunded")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private Integer saleIsRefunded;

    @JsonProperty("sale.paymentFrequency")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    public String getSalePaymentFrequency() {
        return PaymentFrequency.getPaymentFrequency(saleOrigin).toString();
    }

    @JsonProperty("remittance.applicationTransactionId")
    @Override
    public String getApplicationTransactionId() {
        return null;
    }

    @Override
    public String getProcessorTransactionId() {
        return processorTransactionId;
    }

    @Override
    public String getMerchantId() {
        return merchantId;
    }

    @JsonProperty("remittance.transactionDate")
    @Override
    public DateTime getTransactionDateTime() {
        return transactionTime;
    }

    @Transient
    @JsonProperty("ReconDate.Processor_Name")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String Processor_Name;

    @Transient
    @JsonProperty("ReconDate.MID")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String MID;

    @Transient
    @JsonProperty("ReconDate.ReconciliationStatus_ID")
    @JsonView({ Views.Extend.class, Views.Summary.class })
    private String ReconciliationStatus_ID;
}
