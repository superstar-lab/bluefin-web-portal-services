package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class InternalCodeResource {

    @NotBlank(message = "code value into Internal Code object cannot be null or empty")
    private String code;
    @NotBlank(message = "description value into Internal Code object cannot be null or empty")
    private String description;
    @NotNull(message = "transactionTypeId cannot be null")
    private Long transactionTypeId;
    
    @Valid
    private PaymentProcessorCodeResource paymentProcessorCode;
}
