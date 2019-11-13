package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorRuleId")
public class PaymentProcessorRuleTrendsRequest implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 255255719776826551L;
	
	@NotBlank
	private String frequencyType;
	
	@NotBlank
	private String startDate;
	
	@NotBlank
	private String endDate;
}
