package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PaymentProcessorCodeResource {

    private String code;
    private String description;
    @NotNull(message = "Attribute paymentProcessorId cannot be empty or null")
    private Long paymentProcessorId;
    private Long paymentProcessorCodeId;
}
