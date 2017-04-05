package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

public interface PaymentProcessorInternalResponseCodeDAO {
	public  com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode save(com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponse);

	public  com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode findOne(long paymentProcessorInternalResponseCodeId);

	public  void delete(com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode);

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>  findAll();
	
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodeId(long internalResponseCodeId);
	
	public void createPaymentProcessorInternalStatusCode(Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes);

	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> findPaymentProcessorInternalResponseCodeListById(Long paymentProcessorresponseCodeId);
	
	public void  deleteByInternalResponseCode(Long internalResponseCode);
	
	public List<Long> findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode(Long internalResponseCode);
	
	public void deletePaymentProcessorResponseCodeIds(List<Long> ids);
	
	public  void delete(Long paymentProcessorInternalResponseCodeId);
	
	public  void savePaymentProcessorInternalResponseCodes(Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes);

	public void deletePaymentProcessorInternalResponseCodeForPaymentProcessor(Long paymentProcessorId);
	
	public void deletePaymentProcessorInternalResponseCodeIds(List<Long> paymentProcessorInternalStatusCodeIds);
	public void deleteInternalResponseCodeIds(List<Long> internalStatusCodeIds);
	
}

