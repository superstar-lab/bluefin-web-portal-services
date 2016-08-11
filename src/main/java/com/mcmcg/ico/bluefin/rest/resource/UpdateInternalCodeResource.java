package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdateInternalCodeResource {

    @NotNull(message = "internalCodeId cannot be null")
    private Long internalCodeId;
    @NotBlank(message = "code value into Internal Code object cannot be null or empty")
    private String code;
    @NotBlank(message = "description value into Internal Code object cannot be null or empty")
    private String description;
    @NotBlank(message = "transactionTypeName cannot be null or empty")
    private String transactionTypeName;

    @Valid
    private List<PaymentProcessorCodeResource> paymentProcessorCodes;
}
