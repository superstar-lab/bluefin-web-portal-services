package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class PaymentProcessorRuleTrends implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 255255719776826551L;
	private String frequencyType;
  
	private List<PaymentProcessorRuleDateWiseTrends> paymentProcessorRuleDateWiseTrends;
}
