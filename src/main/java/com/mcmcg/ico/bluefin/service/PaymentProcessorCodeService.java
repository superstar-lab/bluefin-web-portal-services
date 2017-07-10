package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorResponseCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorStatusCodeDAO;
import com.mcmcg.ico.bluefin.rest.resource.ItemStatusCodeResource;

@Service
@Transactional
public class PaymentProcessorCodeService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorCodeService.class);
	
	@Autowired
	private PaymentProcessorResponseCodeDAO paymentProcessorResponseCodeDAO;
	
	@Autowired
	private PaymentProcessorStatusCodeDAO paymentProcessorStatusCodeDAO;
	
	@Autowired
	private TransactionTypeService transactionTypeService;

	public List<ItemStatusCodeResource> hasResponseCodesAssociated(PaymentProcessor paymentProcessor) {
		List<ItemStatusCodeResource> result = new ArrayList<>();
		List<TransactionType> transactionTypes = transactionTypeService.getTransactionTypes();
		LOGGER.debug("hasResponseCodesAssociated() : transactionTypes size :{} ",transactionTypes.size());
		for (TransactionType type : transactionTypes) {
			ItemStatusCodeResource paymentProcessorStatusCodeResource = new ItemStatusCodeResource();
			List<PaymentProcessorResponseCode> responseCodes = paymentProcessorResponseCodeDAO
					.findByTransactionTypeNameAndPaymentProcessor(type.getTransactionTypeName(), paymentProcessor);

			paymentProcessorStatusCodeResource.setTransactionType(type.getTransactionTypeName());
			paymentProcessorStatusCodeResource.setCompleted(hasInternalResponseCodesAssociated(responseCodes));
			result.add(paymentProcessorStatusCodeResource);
		}
		return result;
	}

	public boolean hasInternalResponseCodesAssociated(List<PaymentProcessorResponseCode> responseCodes) {
		LOGGER.debug("Entering to hasInternalResponseCodesAssociated() : responseCodes size : {}",responseCodes.size());
		for (PaymentProcessorResponseCode code : responseCodes) {
//			if (code.getInternalResponseCode() != null && !code.getInternalResponseCode().isEmpty()) {
				return true;
//			}
		}
		return false;
	}

	public List<ItemStatusCodeResource> hasStatusCodesAssociated(PaymentProcessor paymentProcessor) {
		List<ItemStatusCodeResource> result = new ArrayList<>();
		List<TransactionType> transactionTypes = transactionTypeService.getTransactionTypes();
		LOGGER.debug("PaymentProcessorCodeService :: hasStatusCodesAssociated() : transactionTypes : "+transactionTypes.size());
		for (TransactionType type : transactionTypes) {
			ItemStatusCodeResource paymentProcessorStatusCodeResource = new ItemStatusCodeResource();
			List<PaymentProcessorStatusCode> responseCodes = paymentProcessorStatusCodeDAO
					.findByTransactionTypeNameAndPaymentProcessor(type.getTransactionTypeName(), paymentProcessor);
			paymentProcessorStatusCodeResource.setTransactionType(type.getTransactionTypeName());
			paymentProcessorStatusCodeResource.setCompleted(hasInternalStatusCodesAssociated(responseCodes));
			result.add(paymentProcessorStatusCodeResource);
		}
		return result;
	}

	public boolean hasInternalStatusCodesAssociated(List<PaymentProcessorStatusCode> statusCodes) {
		LOGGER.debug("PaymentProcessorCodeService ::Entering to hasInternalStatusCodesAssociated() : statusCodes size : "+statusCodes.size());
		for (PaymentProcessorStatusCode code : statusCodes) {
			if (code.getInternalStatusCode() != null && !code.getInternalStatusCode().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void deletePaymentProcessorStatusCode(Long paymentProcessorId) {
		LOGGER.info("PaymentProcessorCodeService ::Entering to deletePaymentProcessorStatusCode() ");
		paymentProcessorStatusCodeDAO.deletePaymentProcessorStatusCode(paymentProcessorId);
	}
}
