package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
@FunctionalInterface
public interface PaymentProcessorRemittanceDAO {
	PaymentProcessorRemittance findByProcessorTransactionId(String transactionId);
}
