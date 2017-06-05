/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.List;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorResponseCodeDAO {

	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorResponseCode, String transactionTypeName,
			com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor);

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> findByTransactionTypeNameAndPaymentProcessor(String transactionTypeName,
			com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor);

	public void deletePaymentProcessorResponseCode(Long paymentProcessorId);

	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode save(com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode);

	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode findOne(Long paymentProcessorCodeId);
	
	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode update(com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode);
}
