package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class PaymentProcessorResponseCodeResource {

    @NotBlank(message = "code value into Payment Processor Response Code object cannot be null or empty")
    private String code;
    @NotBlank(message = "description value into Payment Processor Response Code object cannot be null or empty")
    private String description;
    @NotNull(message = "Attribute paymentProcessorId cannot be empty or null")
    private Long paymentProcessorId;
}