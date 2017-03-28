/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant;

/**
 * @author mmishra
 *
 */
public interface PaymentProcessorMerchantDAO {
	
	/**
	 * This method is used to fetch PaymentProcessor based on payment processor Id.
	 * 
	 * @param id - type of <Long>.
	 * @return paymentProcessor - type of <PaymentProcessor>.
	 */
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> findPaymentProccessorMerchantByProcessorId(Long paymentProcessorId);

	public void deletPaymentProcessorMerchantByProcID(Long paymentProcessorId);

	public void deletePaymentProcessorRules(Long paymentProcessorId);
	
	public void createPaymentProcessorMerchants(Collection<PaymentProcessorMerchant> paymentProcessorMerchants);

}
