package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class TransactionType implements Serializable {

	private static final long serialVersionUID = -1192867359830305926L;

	public static enum TransactionTypeCode {
		SALE("SALE"), VOID("VOID"), REFUND("REFUND"), REMITTANCE("REMITTANCE"), TOKENIZE("TOKENIZE");

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
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();
	@JsonIgnore
	private DateTime dateModified;
	@JsonIgnore
	private String modifiedBy;

	public TransactionType() {
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

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public DateTime getDateModified() {
		return dateModified;
	}

	public void setDateModified(DateTime dateModified) {
		this.dateModified = dateModified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
}
