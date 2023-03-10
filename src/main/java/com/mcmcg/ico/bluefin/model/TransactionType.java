package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

public class TransactionType extends Common implements Serializable {

	private static final long serialVersionUID = -1192867359830305926L;

	public enum TransactionTypeCode {
		SALE("SALE"), VOID("VOID"), REFUND("REFUND"), REMITTANCE("REMITTANCE"), TOKENIZE("TOKENIZE"),SETTLE("SETTLE"),;

		private final String type;

		private TransactionTypeCode(final String value) {
			this.type = value;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	private Long transactionTypeId;
	private String transactionTypeName;
	private String description;

	public TransactionType() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof TransactionType)) {
			return false;
		}
		TransactionType transactionType = (TransactionType) o;
		return transactionTypeId == transactionType.transactionTypeId
				&& Objects.equals(transactionType, transactionType.transactionTypeName)
				&& Objects.equals(description, transactionType.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(transactionTypeId, transactionTypeName, description);
	}

	public Long getTransactionTypeId() {
		return transactionTypeId;
	}

	public void setTransactionTypeId(Long transactionTypeId) {
		this.transactionTypeId = transactionTypeId;
	}

	public String getTransactionTypeName() {
		return transactionTypeName;
	}

	public void setTransactionTypeName(String transactionTypeName) {
		this.transactionTypeName = transactionTypeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
