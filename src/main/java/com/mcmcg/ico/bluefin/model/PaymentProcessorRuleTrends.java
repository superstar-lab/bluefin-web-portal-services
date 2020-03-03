package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.List;

public class PaymentProcessorRuleTrends implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 255255719776826551L;
	private String frequencyType;
  
	private List<PaymentProcessorRuleDateWiseTrends> paymentProcessorRuleDateWiseTrends;

	public String getFrequencyType() {
		return frequencyType;
	}

	public void setFrequencyType(String frequencyType) {
		this.frequencyType = frequencyType;
	}

	public List<PaymentProcessorRuleDateWiseTrends> getPaymentProcessorRuleDateWiseTrends() {
		return paymentProcessorRuleDateWiseTrends;
	}

	public void setPaymentProcessorRuleDateWiseTrends(
			List<PaymentProcessorRuleDateWiseTrends> paymentProcessorRuleDateWiseTrends) {
		this.paymentProcessorRuleDateWiseTrends = paymentProcessorRuleDateWiseTrends;
	}
}
