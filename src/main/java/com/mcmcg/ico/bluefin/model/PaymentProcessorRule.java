package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorRuleId")
public class PaymentProcessorRule implements Serializable {
    private static final long serialVersionUID = 255255719776828551L;

    private Long paymentProcessorRuleId;

    @JsonProperty(value = "paymentProcessorId")
    @JsonIdentityReference(alwaysAsId = true)
    private PaymentProcessor paymentProcessor;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CardType cardType;

    private BigDecimal maximumMonthlyAmount = BigDecimal.ZERO;

    private Short noMaximumMonthlyAmountFlag = (short) 0;

    private Short priority;

    private BigDecimal monthToDateCumulativeAmount;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;

    @JsonIgnore
    private String lastModifiedBy;

    @JsonIgnore
    public boolean hasNoLimit() {
        return noMaximumMonthlyAmountFlag.equals((short) 1);
    }

	@Override
	public String toString() {
		return "PaymentProcessorRule [paymentProcessorRuleId=" + paymentProcessorRuleId + ", paymentProcessor="
				+ paymentProcessor + ", cardType=" + cardType + ", maximumMonthlyAmount=" + maximumMonthlyAmount
				+ ", noMaximumMonthlyAmountFlag=" + noMaximumMonthlyAmountFlag + ", priority=" + priority
				+ ", monthToDateCumulativeAmount=" + monthToDateCumulativeAmount + "]";
	}

	public Long getPaymentProcessorRuleId() {
		return paymentProcessorRuleId;
	}

	public void setPaymentProcessorRuleId(Long paymentProcessorRuleId) {
		this.paymentProcessorRuleId = paymentProcessorRuleId;
	}

	public PaymentProcessor getPaymentProcessor() {
		return paymentProcessor;
	}

	public void setPaymentProcessor(PaymentProcessor paymentProcessor) {
		this.paymentProcessor = paymentProcessor;
	}

	public CardType getCardType() {
		return cardType;
	}

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	public BigDecimal getMaximumMonthlyAmount() {
		return maximumMonthlyAmount;
	}

	public void setMaximumMonthlyAmount(BigDecimal maximumMonthlyAmount) {
		this.maximumMonthlyAmount = maximumMonthlyAmount;
	}

	public Short getNoMaximumMonthlyAmountFlag() {
		return noMaximumMonthlyAmountFlag;
	}

	public void setNoMaximumMonthlyAmountFlag(Short noMaximumMonthlyAmountFlag) {
		this.noMaximumMonthlyAmountFlag = noMaximumMonthlyAmountFlag;
	}

	public Short getPriority() {
		return priority;
	}

	public void setPriority(Short priority) {
		this.priority = priority;
	}

	public BigDecimal getMonthToDateCumulativeAmount() {
		return monthToDateCumulativeAmount;
	}

	public void setMonthToDateCumulativeAmount(BigDecimal monthToDateCumulativeAmount) {
		this.monthToDateCumulativeAmount = monthToDateCumulativeAmount;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	
}
