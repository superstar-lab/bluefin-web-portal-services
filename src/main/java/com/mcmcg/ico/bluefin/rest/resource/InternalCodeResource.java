package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTransactionTypeName() {
		return transactionTypeName;
	}
	public void setTransactionTypeName(String transactionTypeName) {
		this.transactionTypeName = transactionTypeName;
	}
	public String getInternalStatusCategory() {
		return internalStatusCategory;
	}
	public void setInternalStatusCategory(String internalStatusCategory) {
		this.internalStatusCategory = internalStatusCategory;
	}
	public String getInternalStatusCategoryAbbr() {
		return internalStatusCategoryAbbr;
	}
	public void setInternalStatusCategoryAbbr(String internalStatusCategoryAbbr) {
		this.internalStatusCategoryAbbr = internalStatusCategoryAbbr;
	}
	public List<PaymentProcessorCodeResource> getPaymentProcessorCodes() {
		return paymentProcessorCodes;
	}
	public void setPaymentProcessorCodes(List<PaymentProcessorCodeResource> paymentProcessorCodes) {
		this.paymentProcessorCodes = paymentProcessorCodes;
	}
    
    
}
