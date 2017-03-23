package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.mcmcg.ico.bluefin.rest.resource.Views;

public class PaymentProcessorRemittance implements Serializable, Transaction {

	private static final long serialVersionUID = -7696931237337114459L;

	public PaymentProcessorRemittance() {
	}

	public PaymentProcessorRemittance(Long paymentProcessorRemittanceId, DateTime dateCreated,
			Long reconciliationStatusId, DateTime reconciliationDate, String paymentMethod,
			BigDecimal transactionAmount, String transactionType, DateTime transactionTime, String accountId,
			String application, String processorTransactionId, String merchantId, String transactionSource,
			String firstName, String lastName, DateTime remittanceCreationDate, Long paymentProcessorId,
			String reProcessStatus, Long etlRunId) {
		this.paymentProcessorRemittanceId = paymentProcessorRemittanceId;
		this.dateCreated = dateCreated;
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
		this.reProcessStatus = reProcessStatus;
		this.etlRunId = etlRunId;
	}

	private Long paymentProcessorRemittanceId;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated;
	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long reconciliationStatusId;
	@JsonView({ Views.Extend.class, Views.Summary.class })
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private DateTime reconciliationDate;
	private String paymentMethod;
	private BigDecimal transactionAmount;
	private String transactionType;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime transactionTime;
	private String accountId;
	private String application;
	private String processorTransactionId;
	private String merchantId;
	private String transactionSource;
	private String firstName;
	private String lastName;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private DateTime remittanceCreationDate;
	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long paymentProcessorId;
	@JsonIgnore
	private String reProcessStatus;
	@JsonIgnore
	private Long etlRunId;

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PaymentProcessorRemittance)) {
			return false;
		}
		PaymentProcessorRemittance paymentProcessorRemittance = (PaymentProcessorRemittance) o;
		return paymentProcessorRemittanceId == paymentProcessorRemittance.paymentProcessorRemittanceId
				&& reconciliationStatusId == paymentProcessorRemittance.reconciliationStatusId
				&& Objects.equals(paymentMethod, paymentProcessorRemittance.paymentMethod)
				&& Objects.equals(processorTransactionId, paymentProcessorRemittance.processorTransactionId)
				&& paymentProcessorId == paymentProcessorRemittance.paymentProcessorId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(paymentProcessorRemittanceId, reconciliationStatusId, paymentMethod, processorTransactionId,
				paymentProcessorId);
	}

	public Long getPaymentProcessorRemittanceId() {
		return paymentProcessorRemittanceId;
	}

	public void setPaymentProcessorRemittanceId(Long paymentProcessorRemittanceId) {
		this.paymentProcessorRemittanceId = paymentProcessorRemittanceId;
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

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public DateTime getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(DateTime transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getProcessorTransactionId() {
		return processorTransactionId;
	}

	public void setProcessorTransactionId(String processorTransactionId) {
		this.processorTransactionId = processorTransactionId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getTransactionSource() {
		return transactionSource;
	}

	public void setTransactionSource(String transactionSource) {
		this.transactionSource = transactionSource;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public DateTime getRemittanceCreationDate() {
		return remittanceCreationDate;
	}

	public void setRemittanceCreationDate(DateTime remittanceCreationDate) {
		this.remittanceCreationDate = remittanceCreationDate;
	}

	public Long getPaymentProcessorId() {
		return paymentProcessorId;
	}

	public void setPaymentProcessorId(Long paymentProcessorId) {
		this.paymentProcessorId = paymentProcessorId;
	}

	public String getReProcessStatus() {
		return reProcessStatus;
	}

	public void setReProcessStatus(String reProcessStatus) {
		this.reProcessStatus = reProcessStatus;
	}

	public Long getEtlRunId() {
		return etlRunId;
	}

	public void setEtlRunId(Long etlRunId) {
		this.etlRunId = etlRunId;
	}

	@Override
	public String getApplicationTransactionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime getTransactionDateTime() {
		// TODO Auto-generated method stub
		return null;
	}
}
