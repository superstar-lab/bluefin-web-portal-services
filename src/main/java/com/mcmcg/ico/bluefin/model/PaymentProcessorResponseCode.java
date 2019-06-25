package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentProcessorResponseCode extends CommonPaymentProcessorResponseStatus implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    private Long paymentProcessorResponseCodeId;
    @JsonProperty("paymentProcessorResponseCode")
    private String paymentProcessorResponseCodeValue;
    private String paymentProcessorResponseCodeDescription;
    @JsonIgnore
    private Collection<PaymentProcessorInternalResponseCode> internalResponseCode;
	public Long getPaymentProcessorResponseCodeId() {
		return paymentProcessorResponseCodeId;
	}
	public void setPaymentProcessorResponseCodeId(Long paymentProcessorResponseCodeId) {
		this.paymentProcessorResponseCodeId = paymentProcessorResponseCodeId;
	}
	public String getPaymentProcessorResponseCodeValue() {
		return paymentProcessorResponseCodeValue;
	}
	public void setPaymentProcessorResponseCodeValue(String paymentProcessorResponseCodeValue) {
		this.paymentProcessorResponseCodeValue = paymentProcessorResponseCodeValue;
	}
	public String getPaymentProcessorResponseCodeDescription() {
		return paymentProcessorResponseCodeDescription;
	}
	public void setPaymentProcessorResponseCodeDescription(String paymentProcessorResponseCodeDescription) {
		this.paymentProcessorResponseCodeDescription = paymentProcessorResponseCodeDescription;
	}
	public Collection<PaymentProcessorInternalResponseCode> getInternalResponseCode() {
		return internalResponseCode;
	}
	public void setInternalResponseCode(Collection<PaymentProcessorInternalResponseCode> internalResponseCode) {
		this.internalResponseCode = internalResponseCode;
	}

    
}
