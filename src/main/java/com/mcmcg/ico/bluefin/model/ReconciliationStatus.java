package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ReconciliationStatus extends Common implements Serializable {

	private static final long serialVersionUID = 4957286211246187153L;

	private Long reconciliationStatusId;
	@JsonProperty("reconciliationStatus")
	private String reconciliationStatusValue;
	private String description;
	
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
	
}
