/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorThresholdDAO {
	
	/**
	 * This method is used to fetch PaymentProcessor based on payment processor Id.
	 * 
	 * @param id - type of <Long>.
	 * @return paymentProcessor - type of <PaymentProcessor>.
	 */

	public com.mcmcg.ico.bluefin.model.PaymentProcessorThreshold save(com.mcmcg.ico.bluefin.model.PaymentProcessorThreshold paymentProcessorThreshold);
	
	public com.mcmcg.ico.bluefin.model.PaymentProcessorThreshold updatepaymentProcessorThreshold(
			com.mcmcg.ico.bluefin.model.PaymentProcessorThreshold paymentProcessorThreshold);

}
