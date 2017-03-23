/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.List;
import java.util.Set;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorDAO {
	
	/**
	 * This method is used to fetch PaymentProcessor based on payment processor Id.
	 * 
	 * @param id - type of <Long>.
	 * @return paymentProcessor - type of <PaymentProcessor>.
	 */
	public PaymentProcessor findByPaymentProcessorId(Long paymentProcessorId);
	
	List<com.mcmcg.ico.bluefin.model.PaymentProcessor> findAll();
	
	public void delete(com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor);

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessor> findAll(Set<Long> paymentProcessorIds);

	public com.mcmcg.ico.bluefin.model.PaymentProcessor getPaymentProcessorByProcessorName(String processorName);
	
	public com.mcmcg.ico.bluefin.model.PaymentProcessor save(com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor);

	public PaymentProcessor update(PaymentProcessor paymentProcessorToUpdate);

}
