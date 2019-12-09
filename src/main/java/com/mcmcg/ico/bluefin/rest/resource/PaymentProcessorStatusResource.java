package com.mcmcg.ico.bluefin.rest.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PaymentProcessorStatusResource {
    private ItemStatusResource hasPaymentProcessorName;
    private ItemStatusResource hasSameDayProcessing;
    private ItemStatusResource hasMerchantsAssociated;
    private ItemStatusResource hasRulesAssociated;
    private ItemStatusResource hasResponseCodesAssociated;
    private ItemStatusResource hasStatusCodesAssociated;
	public ItemStatusResource getHasPaymentProcessorName() {
		return hasPaymentProcessorName;
	}
	public void setHasPaymentProcessorName(ItemStatusResource hasPaymentProcessorName) {
		this.hasPaymentProcessorName = hasPaymentProcessorName;
	}
	public ItemStatusResource getHasSameDayProcessing() {
		return hasSameDayProcessing;
	}
	public void setHasSameDayProcessing(ItemStatusResource hasSameDayProcessing) {
		this.hasSameDayProcessing = hasSameDayProcessing;
	}
	public ItemStatusResource getHasMerchantsAssociated() {
		return hasMerchantsAssociated;
	}
	public void setHasMerchantsAssociated(ItemStatusResource hasMerchantsAssociated) {
		this.hasMerchantsAssociated = hasMerchantsAssociated;
	}
	public ItemStatusResource getHasRulesAssociated() {
		return hasRulesAssociated;
	}
	public void setHasRulesAssociated(ItemStatusResource hasRulesAssociated) {
		this.hasRulesAssociated = hasRulesAssociated;
	}
	public ItemStatusResource getHasResponseCodesAssociated() {
		return hasResponseCodesAssociated;
	}
	public void setHasResponseCodesAssociated(ItemStatusResource hasResponseCodesAssociated) {
		this.hasResponseCodesAssociated = hasResponseCodesAssociated;
	}
	public ItemStatusResource getHasStatusCodesAssociated() {
		return hasStatusCodesAssociated;
	}
	public void setHasStatusCodesAssociated(ItemStatusResource hasStatusCodesAssociated) {
		this.hasStatusCodesAssociated = hasStatusCodesAssociated;
	}
    
    
}
