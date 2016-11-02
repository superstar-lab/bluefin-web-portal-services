package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PaymentProcessorCodeResource {

    private String code;
    private String description;
    @NotNull(message = "Please provide a payment processor id for the payment processor code")
    private Long paymentProcessorId;
    private Long paymentProcessorCodeId;
}
