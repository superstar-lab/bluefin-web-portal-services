package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorThresholdId")
public class PaymentProcessorThreshold implements Serializable {
	
    private static final long serialVersionUID = 255255719776827551L;

	private Long paymentProcessorThresholdId;

    private BigDecimal creditAmountThreshold = BigDecimal.ZERO;
    
    private BigDecimal debitAmountThreshold = BigDecimal.ZERO;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<PaymentProcessorRule> paymentProcessorRuleList;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;

    @JsonIgnore
    private String lastModifiedBy;

  

	@Override
	public String toString() {
		return "PaymentProcessorRule [paymentProcessorThresholdId=" + paymentProcessorThresholdId + ", creditAmountThreshold="
				+ creditAmountThreshold + ", debitAmountThreshold=" + debitAmountThreshold +  "]";
	}

}
