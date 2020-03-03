package com.mcmcg.ico.bluefin.model;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mcmcg.ico.bluefin.rest.resource.Views;

public class CommonTransaction implements Transaction {
	private String merchantId;
	private String processorTransactionId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime transactionDateTime = new DateTime();
	private String applicationTransactionId;
	private String application;
	private String processUser;
	private String originalSaleTransactionId;
	private String paymentProcessorStatusCode;
	private String paymentProcessorStatusCodeDescription;
	private String paymentProcessorResponseCode;
	private String paymentProcessorResponseCodeDescription;
	private String internalStatusCode;
	private String internalStatusDescription;
	private String internalResponseCode;
	private String internalResponseDescription;
	private Long paymentProcessorInternalStatusCodeId;
	private Long paymentProcessorInternalResponseCodeId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();
	@JsonView({ Views.Summary.class })
	private Long binDBId;
	@JsonProperty("bindb-detail")
	@JsonView({ Views.Extend.class })
	private BinDBDetails binDBDetails = new BinDBDetails();
	public CommonTransaction() {
		// Default Constructor
	}

	public String getMerchantId() {
		return merchantId;
	}
	public String getProcessorTransactionId() {
		return processorTransactionId;
	}
	public DateTime getTransactionDateTime() {
		return transactionDateTime;
	}
	
	public String getApplicationTransactionId() {
		return applicationTransactionId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getProcessUser() {
		return processUser;
	}

	public void setProcessUser(String processUser) {
		this.processUser = processUser;
	}

	public String getOriginalSaleTransactionId() {
		return originalSaleTransactionId;
	}

	public void setOriginalSaleTransactionId(String originalSaleTransactionId) {
		this.originalSaleTransactionId = originalSaleTransactionId;
	}

	public String getPaymentProcessorStatusCode() {
		return paymentProcessorStatusCode;
	}

	public void setPaymentProcessorStatusCode(String paymentProcessorStatusCode) {
		this.paymentProcessorStatusCode = paymentProcessorStatusCode;
	}

	public String getPaymentProcessorStatusCodeDescription() {
		return paymentProcessorStatusCodeDescription;
	}

	public void setPaymentProcessorStatusCodeDescription(String paymentProcessorStatusCodeDescription) {
		this.paymentProcessorStatusCodeDescription = paymentProcessorStatusCodeDescription;
	}

	public String getPaymentProcessorResponseCode() {
		return paymentProcessorResponseCode;
	}

	public void setPaymentProcessorResponseCode(String paymentProcessorResponseCode) {
		this.paymentProcessorResponseCode = paymentProcessorResponseCode;
	}

	public String getPaymentProcessorResponseCodeDescription() {
		return paymentProcessorResponseCodeDescription;
	}

	public void setPaymentProcessorResponseCodeDescription(String paymentProcessorResponseCodeDescription) {
		this.paymentProcessorResponseCodeDescription = paymentProcessorResponseCodeDescription;
	}

	public String getInternalStatusCode() {
		return internalStatusCode;
	}

	public void setInternalStatusCode(String internalStatusCode) {
		this.internalStatusCode = internalStatusCode;
	}

	public String getInternalStatusDescription() {
		return internalStatusDescription;
	}

	public void setInternalStatusDescription(String internalStatusDescription) {
		this.internalStatusDescription = internalStatusDescription;
	}

	public String getInternalResponseCode() {
		return internalResponseCode;
	}

	public void setInternalResponseCode(String internalResponseCode) {
		this.internalResponseCode = internalResponseCode;
	}

	public String getInternalResponseDescription() {
		return internalResponseDescription;
	}

	public void setInternalResponseDescription(String internalResponseDescription) {
		this.internalResponseDescription = internalResponseDescription;
	}

	public Long getPaymentProcessorInternalStatusCodeId() {
		return paymentProcessorInternalStatusCodeId;
	}

	public void setPaymentProcessorInternalStatusCodeId(Long paymentProcessorInternalStatusCodeId) {
		this.paymentProcessorInternalStatusCodeId = paymentProcessorInternalStatusCodeId;
	}

	public Long getPaymentProcessorInternalResponseCodeId() {
		return paymentProcessorInternalResponseCodeId;
	}

	public void setPaymentProcessorInternalResponseCodeId(Long paymentProcessorInternalResponseCodeId) {
		this.paymentProcessorInternalResponseCodeId = paymentProcessorInternalResponseCodeId;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Long getBinDBId() {
		return binDBId;
	}

	public void setBinDBId(Long binDBId) {
		this.binDBId = binDBId;
	}

	public BinDBDetails getBinDBDetails() {
		return binDBDetails;
	}

	public void setBinDBDetails(BinDBDetails binDBDetails) {
		this.binDBDetails = binDBDetails;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setProcessorTransactionId(String processorTransactionId) {
		this.processorTransactionId = processorTransactionId;
	}

	public void setTransactionDateTime(DateTime transactionDateTime) {
		this.transactionDateTime = transactionDateTime;
	}

	public void setApplicationTransactionId(String applicationTransactionId) {
		this.applicationTransactionId = applicationTransactionId;
	}
	
	/**@Override
	public String getTransactionType() {
		return TransactionTypeCode.VOID.toString();
	}*/
	
	
}
