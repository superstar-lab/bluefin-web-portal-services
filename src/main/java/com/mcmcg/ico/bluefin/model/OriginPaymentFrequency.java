package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OriginPaymentFrequency implements Serializable {

	private static final long serialVersionUID = -1783098456734566961L;

	private Long originPaymentFrequencyId;
	private String origin;
	private String paymentFrequency;
	@JsonIgnore
	private DateTime dateCreated;
	@JsonIgnore
	private DateTime dateModified;
	@JsonIgnore
	private String modifiedBy;

	public OriginPaymentFrequency() {
		// Default constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof OriginPaymentFrequency)) {
			return false;
		}
		OriginPaymentFrequency originPaymentFrequency = (OriginPaymentFrequency) o;
		return originPaymentFrequencyId == originPaymentFrequency.originPaymentFrequencyId
				&& Objects.equals(origin, originPaymentFrequency.origin)
				&& Objects.equals(paymentFrequency, originPaymentFrequency.paymentFrequency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(originPaymentFrequencyId, origin, paymentFrequency);
	}

	public Long getOriginPaymentFrequencyId() {
		return originPaymentFrequencyId;
	}

	public void setOriginPaymentFrequencyId(Long originPaymentFrequencyId) {
		this.originPaymentFrequencyId = originPaymentFrequencyId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getPaymentFrequency() {
		return paymentFrequency;
	}

	public void setPaymentFrequency(String paymentFrequency) {
		this.paymentFrequency = paymentFrequency;
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
