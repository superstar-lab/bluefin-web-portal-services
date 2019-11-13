package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class PaymentProcessorRuleDateWiseTrends implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 255255719776826551L;
	
	@JsonIgnore
	private DateTime histroyDateCreation;
	
	private List<PaymentProcessorRule> paymentProcessorRule;
	
	private String  trendsDateHeader;

}
