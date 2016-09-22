package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
                @ColumnResult(name = "PaymentProcessorID", type = Long.class) }) })
@Data
@EqualsAndHashCode(exclude = { "reconciliationStatus", "paymentProcessor" })
@ToString(exclude = { "reconciliationStatus", "paymentProcessor" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PaymentProcessor_Remittance")
public class PaymentProcessorRemittance implements Serializable, Transaction {
	
	private static final long serialVersionUID = -1312687866731930904L;
	
	public PaymentProcessorRemittance() {
	}
	
	public PaymentProcessorRemittance(Long paymentProcessorRemittanceId, DateTime createdDate, Long reconciliationStatusId, DateTime reconciliationDate,
			String paymentMethod, BigDecimal transactionAmount, String transactionType, DateTime transactionTime, String accountID, String application,
			String processorTransactionID, String merchantID, String transactionSource, String firstName, String lastName, DateTime remittanceCreationDate, Long paymentProcessorId) {
		this.paymentProcessorRemittanceId = paymentProcessorRemittanceId;
		this.createdDate = createdDate;
		this.reconciliationDate = reconciliationDate;
		this.paymentMethod = paymentMethod;
		this.transactionAmount = transactionAmount;
		this.transactionType = transactionType;
		this.transactionTime = transactionTime;
		this.accountID = accountID;
		this.application = application;
		this.processorTransactionID = processorTransactionID;
		this.merchantID = merchantID;
		this.transactionSource = transactionSource;
		this.firstName = firstName;
		this.lastName = lastName;
		this.remittanceCreationDate = remittanceCreationDate;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorRemittanceID")
    private Long paymentProcessorRemittanceId;
    
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@Column(name = "DateCreated", insertable = false, updatable = false)
	private DateTime createdDate;
    
	@ManyToOne
	@JoinColumn(name = "ReconciliationStatusID")
	private ReconciliationStatus reconciliationStatus;
    
    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "ReconciliationDate", insertable = false, updatable = false)
    private DateTime reconciliationDate;
    
    @Column(name = "PaymentMethod")
    private String paymentMethod;
    
    @Column(name = "TransactionAmount", columnDefinition = "money")
    private BigDecimal transactionAmount;
    
    @Column(name = "TransactionType")
    private String transactionType;
    
    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "TransactionTime", insertable = false, updatable = false)
    private DateTime transactionTime;
    
    @Column(name = "AccountID")
    private String accountID;
    
    @Column(name = "Application")
    private String application;
    
    @Column(name = "ProcessorTransactionID")
    private String processorTransactionID;
    
    @Column(name = "MerchantID")
    private String merchantID;
    
    @Column(name = "TransactionSource")
    private String transactionSource;
    
    @Column(name = "FirstName")
    private String firstName;
    
    @Column(name = "LastName")
    private String lastName;
    
    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "RemittanceCreationDate", insertable = false, updatable = false)
    private DateTime remittanceCreationDate;
    
    @ManyToOne
	@JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

	@Override
	public String getApplicationTransactionId() {
		return null;
	}

	@Override
	public String getProcessorTransactionId() {
		return null;
	}

	@Override
	public String getMerchantId() {
		return null;
	}

	@Override
	public DateTime getTransactionDateTime() {
		return null;
	}
}
