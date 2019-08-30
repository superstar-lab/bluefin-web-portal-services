package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;

import lombok.Data;

@Data
public class PaymentProcessorRuleResource implements Serializable {
    private static final long serialVersionUID = -3192378815338636933L;

    private Long id;
    
    @NotNull(message = "Please provide a maximum monthly amount for Debit card to set the payment processor rule")
    @Min(value = 0, message = "Attribute maximumMonthlyAmountForDebit shall never be null and must higher than 0")
    private BigDecimal maximumMonthlyAmountForDebit = new BigDecimal("0.00");
    
    @NotNull(message = "Please provide a maximum monthly amount for Credit card to set the payment processor rule")
    @Min(value = 0, message = "Attribute maximumMonthlyAmountForCredit shall never be null and must higher than 0")
    private BigDecimal maximumMonthlyAmountForCredit = new BigDecimal("0.00");
    
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
            rule.setMaximumMonthlyAmountForDebit(maximumMonthlyAmountForDebit);
            rule.setMaximumMonthlyAmountForCredit(maximumMonthlyAmountForCredit);
            rule.setNoMaximumMonthlyAmountFlag(processRuleRes.getNoMaximumMonthlyAmountFlag());
            rule.setTargetPercentage(processRuleRes.getTargetPercentage());
            rule.setTargetAmount(processRuleRes.getTargetAmount());
   //         rule.setPaymentProcessor(processRuleRes.getpay);
            paymentProcessorRuleList.add(rule);
    	}
        return paymentProcessorRuleList;
    }

   /* public PaymentProcessorRule toPaymentProcessorRule(Long paymentProcessorRuleId) {
    	PaymentProcessorRule rule = new PaymentProcessorRule();
        rule.setPaymentProcessorRuleId(paymentProcessorRuleId);
        rule.setCardType(cardType);
        rule.setMaximumMonthlyAmountForDebit(maximumMonthlyAmountForDebit);
        rule.setMaximumMonthlyAmountForCredit(maximumMonthlyAmountForCredit);
        rule.setNoMaximumMonthlyAmountFlag(noMaximumMonthlyAmountFlag);
        rule.setTargetPercentage(targetPercentage);
        rule.setTargetAmount(targetAmount);

        return rule;
    }*/
}
