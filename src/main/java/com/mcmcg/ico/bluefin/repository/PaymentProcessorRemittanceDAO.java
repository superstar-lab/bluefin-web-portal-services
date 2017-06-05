package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;

public interface PaymentProcessorRemittanceDAO {
	PaymentProcessorRemittance findByProcessorTransactionId(String transactionId);
}
