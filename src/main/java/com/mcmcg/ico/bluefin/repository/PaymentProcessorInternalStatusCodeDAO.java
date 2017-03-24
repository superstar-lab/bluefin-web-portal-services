package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;

public interface PaymentProcessorInternalStatusCodeDAO {
	
	public List<PaymentProcessorInternalStatusCode> findAllForInternalStatusCodeId(Long internalStatusCodeId);
	public void save(PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode);
	public void save(List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes);
	public void delete(Long internalStatusCodeId);
	public void deletePaymentProcessorInternalStatusCodeForPaymentProcessor(Long paymentProcessorId);
	public void deletePaymentProcessorInternalStatusCodeIds(List<Long> paymentProcessorInternalStatusCodeIds);
	public void deleteInternalStatusCodeIds(List<Long> internalStatusCodeIds);
	public List<Long> findPaymentProcessorStatusCodeIdsForInternalStatusCodeId(Long internalStatusCodeId);
	public void deletePaymentProcessorStatusCodeIds(List<Long> paymentProcessorStatusCodeIds);
	public void deletePaymentProcessorStatusCodeIds(Long internalStatusCodeId);
}
