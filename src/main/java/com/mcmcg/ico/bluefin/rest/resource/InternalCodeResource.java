package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class InternalCodeResource {

    @NotBlank(message = "code value into Internal Code object cannot be null or empty")
    private String code;
    @NotBlank(message = "description value into Internal Code object cannot be null or empty")
    private String description;
    @Valid
    private PaymentProcessorCodeResource paymentProcessorCode;
}
