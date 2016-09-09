package com.mcmcg.ico.bluefin.rest.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PaymentProcessorStatusResource {
    private ItemStatusResource hasPaymentProcessorName;
    private ItemStatusResource hasSameDayProcessing;
    private ItemStatusResource hasMerchantsAssociated;
    private ItemStatusResource hasRulesAssociated;
    private ItemStatusResource hasResponseCodesAssociated;
    private ItemStatusResource hasStatusCodesAssociated;
}
