package com.mcmcg.ico.bluefin.rest.resource;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcmcg.ico.bluefin.model.CardType;

public class ProcessRuleResource {
	private static final long serialVersionUID = -3192377590338636933L;

	private Long paymentProcessorRuleId;
	
    private int isRuleDeleted = 0;
    
    private int isRuleActive=0;
    
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
    
    
    @JsonIgnore
    public boolean hasNoLimit() {
        return noMaximumMonthlyAmountFlag.equals((short) 1);
    }


	public Long getPaymentProcessorRuleId() {
		return paymentProcessorRuleId;
	}


	public void setPaymentProcessorRuleId(Long paymentProcessorRuleId) {
		this.paymentProcessorRuleId = paymentProcessorRuleId;
	}


	public int getIsRuleDeleted() {
		return isRuleDeleted;
	}


	public void setIsRuleDeleted(int isRuleDeleted) {
		this.isRuleDeleted = isRuleDeleted;
	}


	public int getIsRuleActive() {
		return isRuleActive;
	}


	public void setIsRuleActive(int isRuleActive) {
		this.isRuleActive = isRuleActive;
	}


	public Long getPaymentProcessorId() {
		return paymentProcessorId;
	}


	public void setPaymentProcessorId(Long paymentProcessorId) {
		this.paymentProcessorId = paymentProcessorId;
	}


	public CardType getCardType() {
		return cardType;
	}


	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}


	public Short getNoMaximumMonthlyAmountFlag() {
		return noMaximumMonthlyAmountFlag;
	}


	public void setNoMaximumMonthlyAmountFlag(Short noMaximumMonthlyAmountFlag) {
		this.noMaximumMonthlyAmountFlag = noMaximumMonthlyAmountFlag;
	}


	public BigDecimal getTargetPercentage() {
		return targetPercentage;
	}


	public void setTargetPercentage(BigDecimal targetPercentage) {
		this.targetPercentage = targetPercentage;
	}


	public BigDecimal getConsumedPercentage() {
		return consumedPercentage;
	}


	public void setConsumedPercentage(BigDecimal consumedPercentage) {
		this.consumedPercentage = consumedPercentage;
	}


	public BigDecimal getMonthToDateCumulativeAmount() {
		return monthToDateCumulativeAmount;
	}


	public void setMonthToDateCumulativeAmount(BigDecimal monthToDateCumulativeAmount) {
		this.monthToDateCumulativeAmount = monthToDateCumulativeAmount;
	}


	public BigDecimal getMaximumMonthlyAmount() {
		return maximumMonthlyAmount;
	}


	public void setMaximumMonthlyAmount(BigDecimal maximumMonthlyAmount) {
		this.maximumMonthlyAmount = maximumMonthlyAmount;
	}
}
