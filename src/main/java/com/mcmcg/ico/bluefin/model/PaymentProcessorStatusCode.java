package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentProcessorStatusCode extends CommonPaymentProcessorResponseStatus implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    private Long paymentProcessorStatusCodeId;
    @JsonProperty("paymentProcessorStatusCode")
    private String paymentProcessorStatusCodeValue;
    private String paymentProcessorStatusCodeDescription;
    @JsonIgnore
    private Collection<PaymentProcessorInternalStatusCode> internalStatusCode;
}
