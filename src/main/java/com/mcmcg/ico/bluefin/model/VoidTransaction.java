package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
@Data
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


	
}
