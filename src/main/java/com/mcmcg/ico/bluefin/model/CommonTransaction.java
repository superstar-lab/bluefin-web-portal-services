package com.mcmcg.ico.bluefin.model;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
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
	@JsonProperty("bin-details")
	private BinDBDetails binDBDetails = new BinDBDetails();
	public CommonTransaction() {
		// Default Constructor
	}
	
	@Override
	public String getMerchantId() {
		return merchantId;
	}
	
	@Override
	public String getProcessorTransactionId() {
		return processorTransactionId;
	}
	
	@Override
	public DateTime getTransactionDateTime() {
		return transactionDateTime;
	}
	
	@Override
	public String getApplicationTransactionId() {
		return applicationTransactionId;
	}
	
	/*@Override
	public String getTransactionType() {
		return TransactionTypeCode.VOID.toString();
	}*/
}
