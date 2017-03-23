/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

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
}
