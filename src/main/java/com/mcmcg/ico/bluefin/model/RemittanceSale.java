package com.mcmcg.ico.bluefin.model;

public class RemittanceSale {

	private PaymentProcessorRemittance paymentProcessorRemittance;
	private SaleTransaction saleTransaction;

	public RemittanceSale() {
		// Default Constructor
	}

	public PaymentProcessorRemittance getPaymentProcessorRemittance() {
		return paymentProcessorRemittance;
	}

	public void setPaymentProcessorRemittance(PaymentProcessorRemittance paymentProcessorRemittance) {
		this.paymentProcessorRemittance = paymentProcessorRemittance;
	}

	public SaleTransaction getSaleTransaction() {
		return saleTransaction;
	}

	public void setSaleTransaction(SaleTransaction saleTransaction) {
		this.saleTransaction = saleTransaction;
	}
}
