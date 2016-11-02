package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentFrequency {

    @JsonProperty("Recurring") RECURRING("Recurring"), @JsonProperty("One Time") ONE_TIME("One Time");

    private final String description;

    private PaymentFrequency(final String value) {
        this.description = value;
    }

    @Override
    public String toString() {
        return description;
    }

    public static PaymentFrequency getPaymentFrequency(final String origin) {
    	if (origin != null) {
    		return origin.equalsIgnoreCase(PaymentFrequency.RECURRING.toString()) ? PaymentFrequency.RECURRING : PaymentFrequency.ONE_TIME;
    	}
    	// What is the default?
        return PaymentFrequency.ONE_TIME;
    }
}
