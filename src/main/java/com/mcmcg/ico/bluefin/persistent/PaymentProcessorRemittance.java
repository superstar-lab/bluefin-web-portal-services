package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "reconciliationStatus", "paymentProcessor" })
@ToString(exclude = { "reconciliationStatus", "paymentProcessor" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PaymentProcessor_Remittance")
public class PaymentProcessorRemittance implements Serializable {
	
	private static final long serialVersionUID = -1312687866731930904L;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "RemittanceCreationDate", insertable = false, updatable = false)
    private DateTime remittanceCreationDate;
    
    @ManyToOne
	@JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    public PaymentProcessorRemittance() {
    }

    public PaymentProcessorRemittance(Long value) {
    	paymentProcessorRemittanceId = value;
    }
}
