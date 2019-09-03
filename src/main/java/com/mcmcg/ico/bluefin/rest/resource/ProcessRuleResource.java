package com.mcmcg.ico.bluefin.rest.resource;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.mcmcg.ico.bluefin.model.CardType;

import lombok.Data;

@Data
public class ProcessRuleResource {
	private static final long serialVersionUID = -3192377590338636933L;

	private Long paymentProcessorRuleId;
    private int paymentProcessorRuleIdDelete = 0;
    
    @NotNull(message = "Please provide a payment processor for the payment processor rule")
    private Long paymentProcessorId;

    @NotNull(message = "Please provide a card type for the payment processor rule")
    private CardType cardType;

    @NotNull(message = "Please provide a no maximum monthly amount flag for the payment processor rule")
    @Min(value = 0, message = "Attribute noMaximumMonthlyAmountFlag shall never be null and must be 0 or 1")
    @Max(value = 1, message = "Attribute noMaximumMonthlyAmountFlag shall never be null and must be 0 or 1")
    private Short noMaximumMonthlyAmountFlag = (short) 0;

    @NotNull(message = "Please provide a target percentage for the payment processor rule")
    @Min(value = 0, message = "Attribute targetPercentage shall never be null and must higher than 0")
    private BigDecimal targetPercentage = new BigDecimal("0.00");
    
    private BigDecimal consumedPercentage = new BigDecimal("0.00");
    
    private BigDecimal monthToDateCumulativeAmount = new BigDecimal("0.00");
    
    private BigDecimal maximumMonthlyAmount = new BigDecimal("0.00");
    
    //private Short priority;
}
