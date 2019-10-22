package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class PaymentProcessorRuleDateWiseTrends implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 255255719776826551L;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime histroyDateCreation;
	
	private List<PaymentProcessorRule> paymentProcessorRule;
	
	private String  weekStartEndDate;

}
