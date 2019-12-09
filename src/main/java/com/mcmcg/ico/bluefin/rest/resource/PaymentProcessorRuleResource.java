package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;

public class PaymentProcessorRuleResource implements Serializable {
    private static final long serialVersionUID = -3192378815338636933L;
    
    @NotNull(message = "Please provide a payment processor rule data")
    List <ProcessRuleResource> processRuleResource;

    /**
     * Transform PaymentProcessorRuleResource to PaymentProcessorRule
     * 
     * @return PaymentProcessorRule
     */
    public List<PaymentProcessorRule> toPaymentProcessorRule() {
    	List<PaymentProcessorRule> paymentProcessorRuleList = new ArrayList<>();
    	for(ProcessRuleResource processRuleRes : processRuleResource) {
    		PaymentProcessorRule rule = new PaymentProcessorRule();
        	rule.setPaymentProcessorRuleId(processRuleRes.getPaymentProcessorRuleId());
            rule.setCardType(processRuleRes.getCardType());
            rule.setNoMaximumMonthlyAmountFlag(processRuleRes.getNoMaximumMonthlyAmountFlag());
            rule.setTargetPercentage(processRuleRes.getTargetPercentage());
            rule.setMonthToDateCumulativeAmount(processRuleRes.getMonthToDateCumulativeAmount());
            rule.setConsumedPercentage(processRuleRes.getConsumedPercentage());
            rule.setMaximumMonthlyAmount(processRuleRes.getMaximumMonthlyAmount());
            rule.setIsRuleDeleted(processRuleRes.getIsRuleDeleted());
            rule.setIsRuleActive(processRuleRes.getIsRuleActive());
          //  rule.setPaymentProcessor(processRuleRes.getPaymentProces);
            paymentProcessorRuleList.add(rule);
    	}
        return paymentProcessorRuleList;
    }

	public List<ProcessRuleResource> getProcessRuleResource() {
		return processRuleResource;
	}

	public void setProcessRuleResource(List<ProcessRuleResource> processRuleResource) {
		this.processRuleResource = processRuleResource;
	}   
  
}
