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
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mcmcg.ico.bluefin.rest.resource.Views;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@SqlResultSetMapping(name = "CustomMappingResult", classes = {
        @ConstructorResult(targetClass = SaleTransaction.class, columns = {
                @ColumnResult(name = "SaleTransactionID", type = Long.class),
                @ColumnResult(name = "TransactionType", type = String.class),
                @ColumnResult(name = "LegalEntityApp", type = String.class),
                @ColumnResult(name = "AccountId", type = String.class),
                @ColumnResult(name = "ApplicationTransactionID", type = String.class),
                @ColumnResult(name = "ProcessorTransactionID", type = String.class),
                @ColumnResult(name = "MerchantID", type = String.class),
                @ColumnResult(name = "TransactionDateTime", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "CardNumberFirst6Char", type = String.class),
                @ColumnResult(name = "CardNumberLast4Char", type = String.class),
                @ColumnResult(name = "CardType", type = String.class),
                @ColumnResult(name = "ChargeAmount", type = BigDecimal.class),
                @ColumnResult(name = "ExpiryDate", type = Date.class),
                @ColumnResult(name = "FirstName", type = String.class),
                @ColumnResult(name = "LastName", type = String.class),
                @ColumnResult(name = "Address1", type = String.class),
                @ColumnResult(name = "Address2", type = String.class),
                @ColumnResult(name = "City", type = String.class), @ColumnResult(name = "State", type = String.class),
                @ColumnResult(name = "PostalCode", type = String.class),
                @ColumnResult(name = "Country", type = String.class),
                @ColumnResult(name = "TestMode", type = Short.class),
                @ColumnResult(name = "Token", type = String.class),
                @ColumnResult(name = "Tokenized", type = Short.class),
                @ColumnResult(name = "PaymentProcessorResponseCode", type = String.class),
                @ColumnResult(name = "PaymentProcessorResponseCodeDescription", type = String.class),
                @ColumnResult(name = "ApprovalCode", type = String.class),
                @ColumnResult(name = "InternalResponseCode", type = String.class),
                @ColumnResult(name = "InternalResponseDescription", type = String.class),
                @ColumnResult(name = "InternalStatusCode", type = String.class),
                @ColumnResult(name = "InternalStatusDescription", type = String.class),
                @ColumnResult(name = "PaymentProcessorStatusCode", type = String.class),
                @ColumnResult(name = "PaymentProcessorStatusCodeDescription", type = String.class),
                @ColumnResult(name = "PaymentProcessorRuleID", type = Long.class),
                @ColumnResult(name = "RulePaymentProcessorID", type = Long.class),
                @ColumnResult(name = "RuleCardType", type = String.class),
                @ColumnResult(name = "RuleMaximumMonthlyAmount", type = BigDecimal.class),
                @ColumnResult(name = "RuleNoMaximumMonthlyAmountFlag", type = Short.class),
                @ColumnResult(name = "RulePriority", type = Short.class),
                @ColumnResult(name = "ProcessUser", type = String.class),
                @ColumnResult(name = "Processor", type = String.class),
                @ColumnResult(name = "Application", type = String.class),
                @ColumnResult(name = "Origin", type = String.class),
                @ColumnResult(name = "AccountPeriod", type = String.class),
                @ColumnResult(name = "Desk", type = String.class),
                @ColumnResult(name = "InvoiceNumber", type = String.class),
                @ColumnResult(name = "UserDefinedField1", type = String.class),
                @ColumnResult(name = "UserDefinedField2", type = String.class),
                @ColumnResult(name = "UserDefinedField3", type = String.class),
                @ColumnResult(name = "DateCreated", type = org.jadira.usertype.dateandtime.joda.PersistentDateTime.class),
                @ColumnResult(name = "IsVoided", type = Integer.class),
                @ColumnResult(name = "IsRefunded", type = Integer.class),
                @ColumnResult(name = "PaymentProcessorInternalStatusCodeID", type = Long.class),
                @ColumnResult(name = "PaymentProcessorInternalResponseCodeID", type = Long.class) }) })

@Data
@EqualsAndHashCode(exclude = { "refundedTransactions", "voidedTransactions" })
@ToString(exclude = { "refundedTransactions", "voidedTransactions" })
@Entity
@Table(name = "Sale_Transaction")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "saleTransactionId")
public class SaleTransaction implements Serializable, Transaction {
    private static final long serialVersionUID = 3783586860046594255L;
    private static final String CARD_MASK = "XXXX-XXXX-XXXX-";

    public SaleTransaction() {

    }

    public SaleTransaction(Long saleTransactionId, String transactionType, String legalEntity, String accountNumber,
            String applicationTransactionId, String processorTransactionId, String merchantId,
            DateTime transactionDateTime, String cardNumberFirst6Char, String cardNumberLast4Char, String cardType,
            BigDecimal amount, Date expiryDate, String firstName, String lastName, String address1, String address2,
            String city, String state, String postalCode, String country, Short testMode, String token, Short tokenized,
            String processorResponseCode, String processorResponseCodeDescription, String approvalCode,
            String internalResponseCode, String internalResponseDescription, String internalStatusCode,
            String internalStatusDescription, String paymentProcessorStatusCode,
            String paymentProcessorStatusCodeDescription, Long paymentProcessorRuleId, Long rulePaymentProcessorId,
            String ruleCardType, BigDecimal ruleMaximumMonthlyAmount, Short ruleNoMaximumMonthlyAmountFlag,
            Short rulePriority, String processUser, String processorName, String application, String origin,
            String accountPeriod, String desk, String invoiceNumber, String userDefinedField1, String userDefinedField2,
            String userDefinedField3, DateTime createdDate, Integer isVoided, Integer isRefunded,
            Long paymentProcessorInternalStatusCodeId, Long paymentProcessorInternalResponseCodeId) {
        this.saleTransactionId = saleTransactionId;
        this.transactionType = transactionType;
        this.legalEntity = legalEntity;
        this.accountNumber = accountNumber;
        this.applicationTransactionId = applicationTransactionId;
        this.processorTransactionId = processorTransactionId;
        this.merchantId = merchantId;
        this.transactionDateTime = transactionDateTime;
        this.cardNumberFirst6Char = cardNumberFirst6Char;
        this.cardNumberLast4Char = cardNumberLast4Char;
        this.cardType = cardType;
        this.amount = amount;
        this.expiryDate = expiryDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.testMode = testMode;
        this.token = token;
        this.tokenized = tokenized;
        this.processorResponseCode = processorResponseCode;
        this.processorResponseCodeDescription = processorResponseCodeDescription;
        this.approvalCode = approvalCode;
        this.internalResponseCode = internalResponseCode;
        this.internalResponseDescription = internalResponseDescription;
        this.internalStatusCode = internalStatusCode;
        this.internalStatusDescription = internalStatusDescription;
        this.paymentProcessorStatusCode = paymentProcessorStatusCode;
        this.paymentProcessorStatusCodeDescription = paymentProcessorStatusCodeDescription;
        this.paymentProcessorRuleId = paymentProcessorRuleId;
        this.rulePaymentProcessorId = rulePaymentProcessorId;
        this.ruleCardType = ruleCardType;
        this.ruleMaximumMonthlyAmount = ruleMaximumMonthlyAmount;
        this.ruleNoMaximumMonthlyAmountFlag = ruleNoMaximumMonthlyAmountFlag;
        this.rulePriority = rulePriority;
        this.processUser = processUser;
        this.processorName = processorName;
        this.application = application;
        this.origin = origin;
        this.accountPeriod = accountPeriod;
        this.desk = desk;
        this.invoiceNumber = invoiceNumber;
        this.userDefinedField1 = userDefinedField1;
        this.userDefinedField2 = userDefinedField2;
        this.userDefinedField3 = userDefinedField3;
        this.createdDate = createdDate;
        this.isVoided = isVoided;
        this.isRefunded = isRefunded;
        this.paymentProcessorInternalStatusCodeId = paymentProcessorInternalStatusCodeId;
        this.paymentProcessorInternalResponseCodeId = paymentProcessorInternalResponseCodeId;
    }

    @Id
    @Column(name = "SaleTransactionID")
    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    private Long saleTransactionId;

    // Transaction Detail
    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "TransactionType")
    private String transactionType;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "LegalEntityApp")
    private String legalEntity;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "AccountId")
    private String accountNumber;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "ApplicationTransactionID")
    private String applicationTransactionId;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "ProcessorTransactionID")
    private String processorTransactionId;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "MerchantID")
    private String merchantId;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "TransactionDateTime")
    private DateTime transactionDateTime;

    // Credit Card Information
    @JsonView(Views.ExtendPublic.class)
    @Column(name = "CardNumberFirst6Char")
    private String cardNumberFirst6Char;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "CardNumberLast4Char")
    private String cardNumberLast4Char;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "CardType")
    private String cardType;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "ChargeAmount", columnDefinition = "money")
    private BigDecimal amount;

    @JsonView(Views.ExtendPublic.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/yy")
    @Column(name = "ExpiryDate")
    private Date expiryDate;

    // Billing Address
    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "FirstName")
    private String firstName;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "LastName")
    private String lastName;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "Address1")
    private String address1;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "Address2")
    private String address2;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "City")
    private String city;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "State")
    private String state;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PostalCode")
    private String postalCode;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "Country")
    private String country;

    // Other
    @JsonView(Views.ExtendPublic.class)
    @Column(name = "TestMode")
    private Short testMode;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "Token")
    private String token;

    @Column(name = "Tokenized")
    @JsonIgnore
    private Short tokenized;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorResponseCode")
    private String processorResponseCode;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorResponseCodeDescription")
    private String processorResponseCodeDescription;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "ApprovalCode")
    private String approvalCode;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "InternalResponseCode")
    private String internalResponseCode;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "InternalResponseDescription")
    private String internalResponseDescription;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "InternalStatusCode")
    private String internalStatusCode;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "InternalStatusDescription")
    private String internalStatusDescription;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorStatusCode")
    private String paymentProcessorStatusCode;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorStatusCodeDescription")
    private String paymentProcessorStatusCodeDescription;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorInternalStatusCodeID")
    private Long paymentProcessorInternalStatusCodeId;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorInternalResponseCodeID")
    private Long paymentProcessorInternalResponseCodeId;

    // Rule
    @JsonView(Views.ExtendPublic.class)
    @Column(name = "PaymentProcessorRuleID")
    private Long paymentProcessorRuleId;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "RulePaymentProcessorID")
    private Long rulePaymentProcessorId;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "RuleCardType")
    private String ruleCardType;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "RuleMaximumMonthlyAmount", columnDefinition = "money")
    private BigDecimal ruleMaximumMonthlyAmount;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "RuleNoMaximumMonthlyAmountFlag")
    private Short ruleNoMaximumMonthlyAmountFlag;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "RulePriority")
    private Short rulePriority;

    // Misc
    @JsonView(Views.ExtendPublic.class)
    @Column(name = "ProcessUser")
    private String processUser;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "Processor")
    private String processorName;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "Application")
    private String application;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "Origin")
    private String origin;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "AccountPeriod")
    private String accountPeriod;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "Desk")
    private String desk;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @Column(name = "InvoiceNumber")
    private String invoiceNumber;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "UserDefinedField1")
    private String userDefinedField1;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "UserDefinedField2")
    private String userDefinedField2;

    @JsonView(Views.ExtendPublic.class)
    @Column(name = "UserDefinedField3")
    private String userDefinedField3;

    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

    @JsonIgnore
    @OneToMany(mappedBy = "saleTransaction", fetch = FetchType.LAZY)
    private Collection<RefundTransaction> refundedTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "saleTransaction", fetch = FetchType.LAZY)
    private Collection<VoidTransaction> voidedTransactions;

    @JsonIgnore
    @Transient
    private String transactionId;

    @Transient
    @JsonIgnore
    private Integer isVoided = 0;

    @Transient
    @JsonIgnore
    private Integer isRefunded = 0;

    @JsonProperty("isVoided")
    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    private boolean getIsVoided() {
        return (voidedTransactions != null && !voidedTransactions.isEmpty()) || isVoided > 0;
    }

    @JsonProperty("isRefunded")
    @JsonView({ Views.ExtendPublic.class, Views.Public.class })
    private boolean isRefunded() {
        return (refundedTransactions != null && !refundedTransactions.isEmpty()) || isRefunded > 0;
    }

    @JsonProperty("tokenized")
    @JsonView({ Views.ExtendPublic.class })
    public String getTokenized() {
        return tokenized == 1 ? "YES" : "NO";
    }

    public String getCardNumberLast4Char() {
        return CARD_MASK + cardNumberLast4Char;
    }
}
