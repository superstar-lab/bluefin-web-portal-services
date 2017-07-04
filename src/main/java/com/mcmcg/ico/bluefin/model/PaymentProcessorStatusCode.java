package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentProcessorStatusCode extends Common implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    private Long paymentProcessorStatusCodeId;
    @JsonProperty("paymentProcessorStatusCode")
    private String paymentProcessorStatusCodeValue;
    private String paymentProcessorStatusCodeDescription;
    @JsonIgnore
    private Collection<PaymentProcessorInternalStatusCode> internalStatusCode;
    @JsonIgnore
    private PaymentProcessor paymentProcessor;
    @JsonIgnore
    private String lastModifiedBy;
    private String transactionTypeName;

    @JsonProperty("processorId")
    public Long getProcessorId() {
        return this.paymentProcessor.getPaymentProcessorId();
    }

    @JsonProperty("processorName")
    public String getProcessoName() {
        return this.paymentProcessor.getProcessorName();
    }

}
