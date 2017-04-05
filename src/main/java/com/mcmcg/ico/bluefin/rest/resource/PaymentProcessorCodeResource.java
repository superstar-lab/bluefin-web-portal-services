package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PaymentProcessorCodeResource {

    private String code;
    private String description;
    @NotNull(message = "Please provide a payment processor id for the payment processor code")
    // Value of field used to find already saved payment processors
    private Long paymentProcessorId;
    // Value of field used to find already saved payment processor status code
    private Long paymentProcessorCodeId;
}
