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

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorRuleId")
public class PaymentProcessorRule implements Serializable {
    private static final long serialVersionUID = 255255719776828551L;

    private Long paymentProcessorRuleId;

    @JsonProperty(value = "paymentProcessorId")
    @JsonIdentityReference(alwaysAsId = true)
    private PaymentProcessor paymentProcessor;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CardType cardType;

    private BigDecimal maximumMonthlyAmountForDebit = BigDecimal.ZERO;
    
    private BigDecimal maximumMonthlyAmountForCredit = BigDecimal.ZERO;

    private Short noMaximumMonthlyAmountFlag = (short) 0;

    private BigDecimal monthToDateCumulativeAmount;
    
    private BigDecimal targetPercentage = BigDecimal.ZERO;
    
    private BigDecimal targetAmount = BigDecimal.ZERO;
    
    private BigDecimal consumedPercentage;
    
    private BigDecimal consumedAmount;

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
		return "PaymentProcessorRule [paymentProcessorRuleId=" 
				+ paymentProcessorRuleId + ", paymentProcessor="
				+ paymentProcessor + ", cardType=" + cardType + ", maximumMonthlyAmountForDebit=" 
				+ maximumMonthlyAmountForDebit + ", maximumMonthlyAmountForCredit=" 
				+ maximumMonthlyAmountForCredit + ", noMaximumMonthlyAmountFlag="
				+ noMaximumMonthlyAmountFlag + ", monthToDateCumulativeAmount=" 
				+ monthToDateCumulativeAmount + ", targetPercentage="
				+ targetPercentage + ", targetAmount="
				+ targetAmount
				+ "]";
	}

}
