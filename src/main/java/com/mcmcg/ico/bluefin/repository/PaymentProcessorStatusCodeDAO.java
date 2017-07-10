/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorStatusCodeDAO {

	public PaymentProcessorStatusCode findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorStatusCode, String transactionTypeName,
			PaymentProcessor paymentProcessor);

	public List<PaymentProcessorStatusCode> findByTransactionTypeNameAndPaymentProcessor(
			String transactionTypeName, PaymentProcessor paymentProcessor);

	public PaymentProcessorStatusCode findOne(Long paymentProcessorStatusCode);

	public void deletePaymentProcessorStatusCode(Long paymentProcessorId);
	
	public PaymentProcessorStatusCode save(PaymentProcessorStatusCode paymentProcessorStatusCode);
	
	public PaymentProcessorStatusCode update(PaymentProcessorStatusCode paymentProcessorStatusCode);
	
	public boolean isProcessorStatusCodeMapped(String transactionTypeName,PaymentProcessor paymentProcessor);

}
