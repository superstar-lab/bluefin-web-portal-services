package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRule;

import lombok.Data;

@Data
public class PaymentProcessorRuleResource implements Serializable {
    private static final long serialVersionUID = -3192378815338636933L;

    @NotBlank(message = "Attribute paymentProcessorId cannot be empty or null")
    private Long paymentProcessorId;

    @NotBlank(message = "Attribute cardType cannot be empty or null")
    private CardType cardType;

    @NotBlank(message = "Attribute maximumMonthlyAmount cannot be empty or null")
    private BigDecimal maximumMonthlyAmount = new BigDecimal("0.00");

    @NotBlank(message = "Attribute noMaximumMonthlyAmountFlag cannot be empty or null")
    private Short noMaximumMonthlyAmountFlag = (short) 0;

    @NotBlank(message = "Attribute priority cannot be empty or null")
    private Short priority;

    /**
     * Transform PaymentProcessorRuleResource to PaymentProcessorRule
     * 
     * @return PaymentProcessorRule
     */
    public PaymentProcessorRule toPaymentProcessorRule() {
        PaymentProcessorRule rule = new PaymentProcessorRule();
        rule.setPaymentProcessor(new PaymentProcessor(paymentProcessorId));
        rule.setCardType(cardType);
        rule.setMaximumMonthlyAmount(maximumMonthlyAmount);
        rule.setNoMaximumMonthlyAmountFlag(noMaximumMonthlyAmountFlag);

        return rule;
    }
}
