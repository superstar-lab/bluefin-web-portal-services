package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;

@Data
public class OriginPaymentFrequency extends Common implements Serializable {

	private static final long serialVersionUID = -1783098456734566961L;

	private Long originPaymentFrequencyId;
	private String origin;
	private String paymentFrequency;
	
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
	
}
