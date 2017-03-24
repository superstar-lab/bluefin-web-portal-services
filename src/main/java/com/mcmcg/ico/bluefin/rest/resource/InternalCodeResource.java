package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class InternalCodeResource {

    @NotBlank(message = "Please provide a code for the internal code")
    private String code;
    @NotBlank(message = "Please provide a description for the internal code")
    private String description;
    @NotBlank(message = "Please provide a transaction type name for the internal code")
    private String transactionTypeName;
    private String internalStatusCategory;
    private String internalStatusCategoryAbbr;
    @Valid
    private List<PaymentProcessorCodeResource> paymentProcessorCodes;
}
