package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRule;

import lombok.Data;

@Data
public class PaymentProcessorRuleResource implements Serializable {
    private static final long serialVersionUID = -3192378815338636933L;

    @NotNull(message = "Attribute paymentProcessorId cannot be empty or null")
    private Long paymentProcessorId;

    @NotNull(message = "Attribute cardType cannot be empty or null")
    private CardType cardType;

    @NotNull(message = "Attribute maximumMonthlyAmount cannot be empty or null")
    private BigDecimal maximumMonthlyAmount = new BigDecimal("0.00");

    @NotNull(message = "Attribute noMaximumMonthlyAmountFlag cannot be empty or null")
    private Short noMaximumMonthlyAmountFlag = (short) 0;

    @NotNull(message = "Attribute priority cannot be empty or null")
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
}
