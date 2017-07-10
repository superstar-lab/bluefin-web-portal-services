/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorResponseCodeDAO {

	public PaymentProcessorResponseCode findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorResponseCode, String transactionTypeName,
			PaymentProcessor paymentProcessor);

	public List<PaymentProcessorResponseCode> findByTransactionTypeNameAndPaymentProcessor(String transactionTypeName,
			PaymentProcessor paymentProcessor);

	public void deletePaymentProcessorResponseCode(Long paymentProcessorId);

	public PaymentProcessorResponseCode save(PaymentProcessorResponseCode paymentProcessorResponseCode);

	public PaymentProcessorResponseCode findOne(Long paymentProcessorCodeId);
	
	public PaymentProcessorResponseCode update(PaymentProcessorResponseCode paymentProcessorResponseCode);
	
	public boolean isProcessorResponseCodeMapped(String transactionTypeName,PaymentProcessor paymentProcessor);
}
