package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

public class VoidTransaction extends CommonTransaction implements Serializable {

	private static final long serialVersionUID = -4012667071561471682L;

	private Long voidTransactionId;
	private String saleTransactionId;
	private String approvalCode;
	private String processor;
	private String transactionType;
	private SaleTransaction saleTransaction;
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
