package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;

public class RefundTransaction implements Serializable, Transaction {

	private static final long serialVersionUID = -4788942891015146177L;

	private Long refundTransactionId;
	private String saleTransactionId;
	private String approvalCode;
	private String processor;
	private BigDecimal refundAmount;
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
	private Long reconciliationStatusId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime reconciliationDate = new DateTime();
	private Long etlRunId;

	public RefundTransaction() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof RefundTransaction)) {
			return false;
		}
		RefundTransaction refundTransaction = (RefundTransaction) o;
		return refundTransactionId == refundTransaction.refundTransactionId
				&& saleTransactionId == refundTransaction.saleTransactionId
				&& Objects.equals(approvalCode, refundTransaction.approvalCode)
				&& Objects.equals(processor, refundTransaction.processor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(refundTransactionId, saleTransactionId, approvalCode, processor);
	}

	public Long getRefundTransactionId() {
		return refundTransactionId;
	}

	public void setRefundTransactionId(Long refundTransactionId) {
		this.refundTransactionId = refundTransactionId;
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

	public BigDecimal getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(BigDecimal refundAmount) {
		this.refundAmount = refundAmount;
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

	public Long getReconciliationStatusId() {
		return reconciliationStatusId;
	}

	public void setReconciliationStatusId(Long reconciliationStatusId) {
		this.reconciliationStatusId = reconciliationStatusId;
	}

	public DateTime getReconciliationDate() {
		return reconciliationDate;
	}

	public void setReconciliationDate(DateTime reconciliationDate) {
		this.reconciliationDate = reconciliationDate;
	}

	public Long getEtlRunId() {
		return etlRunId;
	}

	public void setEtlRunId(Long etlRunId) {
		this.etlRunId = etlRunId;
	}

	@Override
	public String getTransactionType() {
		return TransactionTypeCode.REFUND.toString();
	}
}
