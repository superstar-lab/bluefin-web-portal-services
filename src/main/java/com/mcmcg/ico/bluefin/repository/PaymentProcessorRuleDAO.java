/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

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

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findByCardType(String cardType);

	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule save(com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule);

	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule findOne(long id);

	public void delete(Long paymentProcessorRuleId);

	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule updatepaymentProcessorRule(
			com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRuleToUpdate);

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findByPaymentProcessor(Long paymentProcessorId);
}
