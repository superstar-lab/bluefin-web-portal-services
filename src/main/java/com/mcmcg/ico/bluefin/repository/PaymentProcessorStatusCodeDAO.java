/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.List;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorStatusCodeDAO {

	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorStatusCode, String transactionTypeName,
			com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor);

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> findByTransactionTypeNameAndPaymentProcessor(
			String transactionTypeName, com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor);

	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode findOne(Long paymentProcessorStatusCode);

	public void deletePaymentProcessorStatusCode(Long paymentProcessorId);
	
	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode save(com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCode);
	
	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode update(com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCode);

}
