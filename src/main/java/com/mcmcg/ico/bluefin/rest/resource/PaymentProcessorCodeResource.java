package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

public class PaymentProcessorCodeResource {

    private String code;
    private String description;
    @NotNull(message = "Please provide a payment processor id for the payment processor code")
    // Value of field used to find already saved payment processors
    private Long paymentProcessorId;
    // Value of field used to find already saved payment processor status code
    private Long paymentProcessorCodeId;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getPaymentProcessorId() {
		return paymentProcessorId;
	}
	public void setPaymentProcessorId(Long paymentProcessorId) {
		this.paymentProcessorId = paymentProcessorId;
	}
	public Long getPaymentProcessorCodeId() {
		return paymentProcessorCodeId;
	}
	public void setPaymentProcessorCodeId(Long paymentProcessorCodeId) {
		this.paymentProcessorCodeId = paymentProcessorCodeId;
	}
    
    
}
