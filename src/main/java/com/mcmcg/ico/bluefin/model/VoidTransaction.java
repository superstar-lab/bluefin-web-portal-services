package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;

public class VoidTransaction implements Serializable, Transaction {

	private static final long serialVersionUID = -4012667071561471682L;

	private Long voidTransactionId;
	private String saleTransactionId;
	private String approvalCode;
	private String processor;
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

	public VoidTransaction() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof VoidTransaction)) {
			return false;
		}
		VoidTransaction voidTransaction = (VoidTransaction) o;
		return voidTransactionId == voidTransaction.voidTransactionId
				&& saleTransactionId == voidTransaction.saleTransactionId
				&& Objects.equals(approvalCode, voidTransaction.approvalCode)
				&& Objects.equals(processor, voidTransaction.processor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(voidTransactionId, saleTransactionId, approvalCode, processor);
	}

	public Long getVoidTransactionId() {
		return voidTransactionId;
	}

	public void setVoidTransactionId(Long voidTransactionId) {
		this.voidTransactionId = voidTransactionId;
	}

	public String getSaleTransactionId() {
		return saleTransactionId;
	}

	public void setSaleTransactionId(String saleTransactionId) {
		this.saleTransactionId = saleTransactionId;
	}

	public String getApprovalCode() {
		return approvalCode;
	}

	public void setApprovalCode(String approvalCode) {
		this.approvalCode = approvalCode;
	}

	public String getProcessor() {
		return processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}
	@Override
	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	@Override
	public String getProcessorTransactionId() {
		return processorTransactionId;
	}

	public void setProcessorTransactionId(String processorTransactionId) {
		this.processorTransactionId = processorTransactionId;
	}
	@Override
	public DateTime getTransactionDateTime() {
		return transactionDateTime;
	}

	public void setTransactionDateTime(DateTime transactionDateTime) {
		this.transactionDateTime = transactionDateTime;
	}
	@Override
	public String getApplicationTransactionId() {
		return applicationTransactionId;
	}

	public void setApplicationTransactionId(String applicationTransactionId) {
		this.applicationTransactionId = applicationTransactionId;
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

	@Override
	public String getTransactionType() {
		return TransactionTypeCode.VOID.toString();
	}
}
