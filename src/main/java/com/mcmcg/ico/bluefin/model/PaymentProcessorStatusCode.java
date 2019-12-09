package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentProcessorStatusCode extends CommonPaymentProcessorResponseStatus implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    private Long paymentProcessorStatusCodeId;
    @JsonProperty("paymentProcessorStatusCode")
    private String paymentProcessorStatusCodeValue;
    private String paymentProcessorStatusCodeDescription;
    @JsonIgnore
    private Collection<PaymentProcessorInternalStatusCode> internalStatusCode;
	public Long getPaymentProcessorStatusCodeId() {
		return paymentProcessorStatusCodeId;
	}
	public void setPaymentProcessorStatusCodeId(Long paymentProcessorStatusCodeId) {
		this.paymentProcessorStatusCodeId = paymentProcessorStatusCodeId;
	}
	public String getPaymentProcessorStatusCodeValue() {
		return paymentProcessorStatusCodeValue;
	}
	public void setPaymentProcessorStatusCodeValue(String paymentProcessorStatusCodeValue) {
		this.paymentProcessorStatusCodeValue = paymentProcessorStatusCodeValue;
	}
	public String getPaymentProcessorStatusCodeDescription() {
		return paymentProcessorStatusCodeDescription;
	}
	public void setPaymentProcessorStatusCodeDescription(String paymentProcessorStatusCodeDescription) {
		this.paymentProcessorStatusCodeDescription = paymentProcessorStatusCodeDescription;
	}
	public Collection<PaymentProcessorInternalStatusCode> getInternalStatusCode() {
		return internalStatusCode;
	}
	public void setInternalStatusCode(Collection<PaymentProcessorInternalStatusCode> internalStatusCode) {
		this.internalStatusCode = internalStatusCode;
	}
    
    
}
