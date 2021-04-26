package com.mcmcg.ico.bluefin.model;

import lombok.Data;

@Data
public class ApprovedTranSummary {
	private String legalEntity;
	private String numberTransactions;
	private String totalAmount;
	private String processor;
}
