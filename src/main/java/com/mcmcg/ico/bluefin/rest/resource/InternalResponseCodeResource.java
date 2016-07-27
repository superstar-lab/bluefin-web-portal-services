package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class InternalResponseCodeResource {

    @NotBlank(message = "code value into Internal Response Code object cannot be null or empty")
    private String code;
    @NotBlank(message = "description value into Internal Response Code object cannot be null or empty")
    private String description;
    @Valid
    private PaymentProcessorResponseCodeResource paymentProcessorResponseCode;
    @NotNull(message = "Attribute categoryId cannot be empty or null")
    private Long categoryId;
}
