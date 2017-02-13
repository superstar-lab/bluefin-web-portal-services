package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorResponseCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorStatusCodeRepository;
import com.mcmcg.ico.bluefin.rest.resource.ItemStatusCodeResource;

@Service
@Transactional
public class PaymentProcessorCodeService {

	@Autowired
	private PaymentProcessorResponseCodeRepository paymentProcessorResponseCodeRepository;
	@Autowired
	private PaymentProcessorStatusCodeRepository paymentProcessorStatusCodeRepository;
	@Autowired
	private TransactionTypeService transactionTypeService;

	public List<ItemStatusCodeResource> hasResponseCodesAssociated(PaymentProcessor paymentProcessor) {
		List<ItemStatusCodeResource> result = new ArrayList<ItemStatusCodeResource>();
		List<TransactionType> transactionTypes = transactionTypeService.getTransactionTypes();
		for (TransactionType type : transactionTypes) {
			ItemStatusCodeResource paymentProcessorStatusCodeResource = new ItemStatusCodeResource();
			List<PaymentProcessorResponseCode> responseCodes = paymentProcessorResponseCodeRepository
					.findByTransactionTypeNameAndPaymentProcessor(type.getTransactionType(), paymentProcessor);

			paymentProcessorStatusCodeResource.setTransactionType(type.getTransactionType());
			paymentProcessorStatusCodeResource.setCompleted(hasInternalResponseCodesAssociated(responseCodes));
			result.add(paymentProcessorStatusCodeResource);
		}
		return result;
	}

	public boolean hasInternalResponseCodesAssociated(List<PaymentProcessorResponseCode> responseCodes) {
		for (PaymentProcessorResponseCode code : responseCodes) {
			if (code.getInternalResponseCode() != null && !code.getInternalResponseCode().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public List<ItemStatusCodeResource> hasStatusCodesAssociated(PaymentProcessor paymentProcessor) {
		List<ItemStatusCodeResource> result = new ArrayList<ItemStatusCodeResource>();
		List<TransactionType> transactionTypes = transactionTypeService.getTransactionTypes();
		for (TransactionType type : transactionTypes) {
			ItemStatusCodeResource paymentProcessorStatusCodeResource = new ItemStatusCodeResource();
			List<PaymentProcessorStatusCode> responseCodes = paymentProcessorStatusCodeRepository
					.findByTransactionTypeNameAndPaymentProcessor(type.getTransactionType(), paymentProcessor);
			paymentProcessorStatusCodeResource.setTransactionType(type.getTransactionType());
			paymentProcessorStatusCodeResource.setCompleted(hasInternalStatusCodesAssociated(responseCodes));
			result.add(paymentProcessorStatusCodeResource);
		}
		return result;
	}

	public boolean hasInternalStatusCodesAssociated(List<PaymentProcessorStatusCode> statusCodes) {
		for (PaymentProcessorStatusCode code : statusCodes) {
			if (code.getInternalStatusCode() != null && !code.getInternalStatusCode().isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
