package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PaymentProcessorRuleDateWiseTrends implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 255255719776826551L;
	
	@JsonIgnore
	private DateTime histroyDateCreation;
	
	private List<PaymentProcessorRule> paymentProcessorRule;
	
	private String  trendsDateHeader;

	public DateTime getHistroyDateCreation() {
		return histroyDateCreation;
	}

	public void setHistroyDateCreation(DateTime histroyDateCreation) {
		this.histroyDateCreation = histroyDateCreation;
	}

	public List<PaymentProcessorRule> getPaymentProcessorRule() {
		return paymentProcessorRule;
	}

	public void setPaymentProcessorRule(List<PaymentProcessorRule> paymentProcessorRule) {
		this.paymentProcessorRule = paymentProcessorRule;
	}

	public String getTrendsDateHeader() {
		return trendsDateHeader;
	}

	public void setTrendsDateHeader(String trendsDateHeader) {
		this.trendsDateHeader = trendsDateHeader;
	}

}
