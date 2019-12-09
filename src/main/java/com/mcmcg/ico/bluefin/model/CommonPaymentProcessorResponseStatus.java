package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommonPaymentProcessorResponseStatus extends Common {

	@JsonIgnore
    private PaymentProcessor paymentProcessor;
    @JsonIgnore
    private String lastModifiedBy;
    private String transactionTypeName;
 
    public CommonPaymentProcessorResponseStatus() {
    	// Default Constructor
	}
    
    @JsonProperty("processorId")
    public Long getProcessorId() {
        return this.paymentProcessor.getPaymentProcessorId();
    }
 
    @JsonProperty("processorName")
    public String getProcessoName() {
        return this.paymentProcessor.getProcessorName();
    }

	public PaymentProcessor getPaymentProcessor() {
		return paymentProcessor;
	}

	public void setPaymentProcessor(PaymentProcessor paymentProcessor) {
		this.paymentProcessor = paymentProcessor;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getTransactionTypeName() {
		return transactionTypeName;
	}

	public void setTransactionTypeName(String transactionTypeName) {
		this.transactionTypeName = transactionTypeName;
	}
    
	

}
