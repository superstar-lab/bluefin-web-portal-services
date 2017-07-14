package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CommonPaymentProcessorResponseStatus extends Common {

	@JsonIgnore
    private PaymentProcessor paymentProcessor;
    @JsonIgnore
    private String lastModifiedBy;
    private String transactionTypeName;
 
    public CommonPaymentProcessorResponseStatus() {
	}
    
    @JsonProperty("processorId")
    public Long getProcessorId() {
        return this.paymentProcessor.getPaymentProcessorId();
    }
 
    @JsonProperty("processorName")
    public String getProcessoName() {
        return this.paymentProcessor.getProcessorName();
    }
    
	

}
