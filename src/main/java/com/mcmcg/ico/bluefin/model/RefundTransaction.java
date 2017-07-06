package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
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
}
