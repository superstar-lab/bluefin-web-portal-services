package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReconciliationStatus implements Serializable {

	private static final long serialVersionUID = 4957286211246187153L;

	private Long reconciliationStatusId;
	@JsonProperty("reconciliationStatus")
	private String reconciliationStatusValue;
	private String description;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateModified = new DateTime();
	@JsonIgnore
	private String modifiedBy;

	public ReconciliationStatus() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ReconciliationStatus)) {
			return false;
		}
		ReconciliationStatus reconciliationStatus = (ReconciliationStatus) o;
		return reconciliationStatusId == reconciliationStatus.reconciliationStatusId
				&& Objects.equals(reconciliationStatus, reconciliationStatus.reconciliationStatusValue)
				&& Objects.equals(description, reconciliationStatus.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(reconciliationStatusId, reconciliationStatusValue, description);
	}

	public Long getReconciliationStatusId() {
		return reconciliationStatusId;
	}

	public void setReconciliationStatusId(Long reconciliationStatusId) {
		this.reconciliationStatusId = reconciliationStatusId;
	}

	public String getReconciliationStatusValue() {
		return reconciliationStatusValue;
	}

	public void setReconciliationStatusValue(String reconciliationStatus) {
		this.reconciliationStatusValue = reconciliationStatus;
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
