package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRule;

import lombok.Data;

@Data
public class PaymentProcessorRuleResource implements Serializable {
    private static final long serialVersionUID = -3192378815338636933L;

    @NotNull(message = "Please provide a payment processor id for the payment processor rule")
    private Long paymentProcessorId;

    @NotNull(message = "Please provide a card type for the payment processor rule")
    private CardType cardType;

    @NotNull(message = "Please provide a maximum monthly amount for the payment processor rule")
    @Min(value = 0, message = "Attribute maximumMonthlyAmount shall never be null and must higher than 0")
    private BigDecimal maximumMonthlyAmount = new BigDecimal("0.00");

    @NotNull(message = "Please provide a no maximum monthly amount flag for the payment processor rule")
    @Min(value = 0, message = "Attribute noMaximumMonthlyAmountFlag shall never be null and must be 0 or 1")
    @Max(value = 1, message = "Attribute noMaximumMonthlyAmountFlag shall never be null and must be 0 or 1")
    private Short noMaximumMonthlyAmountFlag = (short) 0;

    @NotNull(message = "Please provide a priority for the payment processor rule")
    @Min(value = 1, message = "Attribute priority shall never be null and must be between 1 and " + Short.MAX_VALUE)
    @Max(value = Short.MAX_VALUE, message = "Attribute priority shall never be null and must be between 1 and " + Short.MAX_VALUE)
    private Short priority;

    /**
     * Transform PaymentProcessorRuleResource to PaymentProcessorRule
     * 
     * @return PaymentProcessorRule
     */
    public PaymentProcessorRule toPaymentProcessorRule() {
        PaymentProcessorRule rule = new PaymentProcessorRule();
        rule.setCardType(cardType);
        rule.setMaximumMonthlyAmount(maximumMonthlyAmount);
        rule.setNoMaximumMonthlyAmountFlag(noMaximumMonthlyAmountFlag);
        rule.setPriority(priority);

        return rule;
    }

    public PaymentProcessorRule toPaymentProcessorRule(Long paymentProcessorRuleId) {
        PaymentProcessorRule rule = new PaymentProcessorRule();
        rule.setPaymentProcessorRuleId(paymentProcessorRuleId);
        rule.setCardType(cardType);
        rule.setMaximumMonthlyAmount(maximumMonthlyAmount);
        rule.setNoMaximumMonthlyAmountFlag(noMaximumMonthlyAmountFlag);
        rule.setPriority(priority);

        return rule;
    }
}
