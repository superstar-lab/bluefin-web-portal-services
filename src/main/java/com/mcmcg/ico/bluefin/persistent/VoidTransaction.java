package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "saleTransaction" })
@ToString(exclude = { "saleTransaction" })
@Entity
@Table(name = "Void_Transaction")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "voidTransactionId")
public class VoidTransaction implements Serializable, Transaction {
	private static final long serialVersionUID = -6252148309760154839L;

	@Id
	@Column(name = "VoidTransactionID")
	private Long voidTransactionId;

	@ManyToOne
	@JoinColumn(name = "SaleTransactionID")
	private SaleTransaction saleTransaction;

	// Transaction Detail
	@Transient
	private String transactionType = TransactionTypeCode.VOID.toString();

	@Column(name = "OriginalSaleTransactionID")
	private String originalSaleTransactionID;

	@Column(name = "ApplicationTransactionID")
	private String applicationTransactionId;

	@Column(name = "ProcessorTransactionID")
	private String processorTransactionId;

	@Column(name = "MerchantID")
	private String merchantId;

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
}
