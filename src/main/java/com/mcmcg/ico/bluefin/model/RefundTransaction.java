package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RefundTransaction extends CommonTransaction implements Serializable {

	private static final long serialVersionUID = -4788942891015146177L;

	private Long refundTransactionId;
	private String saleTransactionId;
	private String approvalCode;
	private String processor;
	private BigDecimal refundAmount;
	private Long reconciliationStatusId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime reconciliationDate = new DateTime();
	private Long etlRunId;
	private String transactionType;
	private SaleTransaction saleTransaction;
	
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

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public SaleTransaction getSaleTransaction() {
		return saleTransaction;
	}

	public void setSaleTransaction(SaleTransaction saleTransaction) {
		this.saleTransaction = saleTransaction;
	}
	
	
}
