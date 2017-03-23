/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorRuleDAO {
	
	/**
	 * This method is used to fetch PaymentProcessor based on payment processor Id.
	 * 
	 * @param id - type of <Long>.
	 * @return paymentProcessor - type of <PaymentProcessor>.
	 */
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findPaymentProccessorRulByProcessorId(Long paymentProcessorId);
	
	public void createPaymentProcessorRules(Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorInternalStatusCodes);

	public void deletePaymentProcessorRules(Long paymentProcessorId);

}
