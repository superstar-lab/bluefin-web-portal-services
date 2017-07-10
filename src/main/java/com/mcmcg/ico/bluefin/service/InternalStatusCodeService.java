package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.InternalStatusCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.repository.InternalStatusCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorInternalStatusCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorStatusCodeDAO;
import com.mcmcg.ico.bluefin.repository.TransactionTypeDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateInternalCodeResource;

@Service
@Transactional
public class InternalStatusCodeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalStatusCodeService.class);

	@Autowired
	private InternalStatusCodeDAO internalStatusCodeDAO;
	@Autowired
	private PaymentProcessorInternalStatusCodeDAO paymentProcessorInternalStatusCodeDAO;
	
	@Autowired
	private PaymentProcessorStatusCodeDAO paymentProcessorStatusCodeDAO;
	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;
	@Autowired
	private TransactionTypeDAO transactionTypeDAO;

	public List<InternalStatusCode> getInternalStatusCodesByTransactionType(final String transactionType) {
		List<InternalStatusCode> internalStatusCodeList = internalStatusCodeDAO.findByTransactionTypeNameOrderByInternalStatusCodeAsc(transactionType);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("InternalStatusCodeService :: getInternalStatusCodesByTransactionType() : internalStatusCodeList size : {}",internalStatusCodeList.size());
		}
		if (internalStatusCodeList != null) {
			for(InternalStatusCode internalStatusCode : internalStatusCodeList){
				Long internalStatusCodeId = internalStatusCode.getInternalStatusCodeId();
				List<PaymentProcessorInternalStatusCode> list = paymentProcessorInternalStatusCodeDAO.findAllForInternalStatusCodeId(internalStatusCodeId);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("InternalStatusCodeService :: getInternalStatusCodesByTransactionType() : PaymentProcessorInternalStatusCode size : {} ",list.size());
				}
				for (PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode : list) {
					InternalStatusCode internalStatusCode1 = internalStatusCodeDAO.findOne(paymentProcessorInternalStatusCode.getInternalStatusCodeId());
					paymentProcessorInternalStatusCode.setInternalStatusCode(internalStatusCode1);
					PaymentProcessorStatusCode paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.findOne(paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
					paymentProcessorInternalStatusCode.setPaymentProcessorStatusCode(paymentProcessorStatusCode);
				}
				internalStatusCode.setPaymentProcessorInternalStatusCodes(list);
			}
		}
		return internalStatusCodeList;
	}
	
	private TransactionType validateTransactionType(String transactionTypeName){
		TransactionType transactionType = transactionTypeDAO
				.findByTransactionType(transactionTypeName);
		if (transactionType == null) {
			LOGGER.error("InternalStatusCodeService :: createInternalStatusCodes() : Transaction type {} not found",transactionTypeName);
			throw new CustomBadRequestException("Transaction type not exists.");
		}
		return transactionType;
	}
	
	private void validateInternalStatusCode(String internalStatusCodeResourceCode,String transactionTypeName){
		if (internalStatusCodeDAO
				.findByInternalStatusCodeAndTransactionTypeName(internalStatusCodeResourceCode,
						transactionTypeName) != null) {
			throw new CustomBadRequestException(
					"Internal Status code already exists and is assigned to this transaction type.");
		}
	}
	
	private void validateInternalStatusCode(String currentResponseCode,String resourceProcessorCode,boolean codeModified){
		if (!currentResponseCode.equals(resourceProcessorCode)	&& !codeModified) {
			throw new CustomBadRequestException(
					"This Payment Processor is already related to another Internal Status Code.");
		}
	}
	
	private InternalStatusCode populatenInternalStatusCode(InternalCodeResource internalStatusCodeResource,String transactionTypeName,String currentLoginUserName){
		InternalStatusCode internalStatusCode = new InternalStatusCode();
		internalStatusCode.setInternalStatusCodeValue(internalStatusCodeResource.getCode());
		internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());
		internalStatusCode.setTransactionTypeName(transactionTypeName);
		internalStatusCode.setPaymentProcessorInternalStatusCodes(new ArrayList<PaymentProcessorInternalStatusCode>());
		internalStatusCode.setInternalStatusCategory(internalStatusCodeResource.getInternalStatusCategory());
		internalStatusCode.setInternalStatusCategoryAbbr(internalStatusCodeResource.getInternalStatusCategoryAbbr());
		internalStatusCode.setLastModifiedBy(currentLoginUserName);
		return internalStatusCode;
	}
	
	private PaymentProcessorStatusCode validatePaymentProcessorStatusCode(PaymentProcessorStatusCode paymentProcessorStatusCode,InternalCodeResource internalStatusCodeResource,PaymentProcessorCodeResource resourceProcessorCode,boolean codeModified){
		PaymentProcessorStatusCode paymentProcessorStatusCodeObj;
		if (paymentProcessorStatusCode == null) {
			LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Creating new payment processor Status code {}", resourceProcessorCode.getCode());
			paymentProcessorStatusCodeObj = new PaymentProcessorStatusCode();
		} else {
			Collection<PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
					.getInternalStatusCode();
			if ( currentPaymentProcessorInternalStatusCodes != null ) {
				for (PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode : currentPaymentProcessorInternalStatusCodes) {
					validateStatusCodeChange(currentPaymentProcessorInternalStatusCode,internalStatusCodeResource,codeModified);
				}
			}
			paymentProcessorStatusCodeObj = paymentProcessorStatusCode;
		}
		return paymentProcessorStatusCodeObj;
	}
	
	private void validateStatusCodeChange(PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode,InternalCodeResource internalStatusCodeResource,boolean codeModified){
		if (currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode() != null 
				&& !StringUtils.equals(currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode().getPaymentProcessorStatusCodeValue(),internalStatusCodeResource.getCode())
				&& !codeModified) {
			throw new CustomBadRequestException(
				"This Payment Processor is already assingned to another Internal Status Code.");
		}
	}
	private void validatePaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(PaymentProcessor paymentProcessor,String code,String transactionTypeName){
		if (paymentProcessorStatusCodeDAO
				.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
						code, transactionTypeName,
						paymentProcessor) != null) {
			throw new CustomBadRequestException("The code " + code + " is already used by other Payment Processor Status Code.");
		}
	}
	
	private boolean isCodeModifiedCreateCase(PaymentProcessor paymentProcessor,PaymentProcessorStatusCode paymentProcessorStatusCode,PaymentProcessorCodeResource resourceProcessorCode,String transactionTypeName){
		if (!paymentProcessorStatusCode.getPaymentProcessorStatusCodeValue().equals(resourceProcessorCode.getCode())) {
			validatePaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(paymentProcessor,resourceProcessorCode.getCode(), transactionTypeName);
			return true;
		}
		return false;
	}
	
	public InternalStatusCode createInternalStatusCodes(InternalCodeResource internalStatusCodeResource,String currentLoginUserName) {

		// Get transactionType if null thrown an exception
		TransactionType transactionType = validateTransactionType(internalStatusCodeResource.getTransactionTypeName());
		validateInternalStatusCode(internalStatusCodeResource.getCode(),internalStatusCodeResource.getTransactionTypeName());
		
		LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Creating new internal Status code {}", internalStatusCodeResource.getCode());
		InternalStatusCode internalStatusCode = populatenInternalStatusCode(internalStatusCodeResource,transactionType.getTransactionTypeName(),currentLoginUserName);
		
		if ( internalStatusCodeResource.getPaymentProcessorCodes() != null && !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Number of payment processor internal status codes="+internalStatusCodeResource.getPaymentProcessorCodes().size());
			List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = new ArrayList<>();
			for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource.getPaymentProcessorCodes()) {
				LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Payment processor internal status code="+resourceProcessorCode);
				// validate if payment processor is exists or not
				PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());
				if (paymentProcessor == null) {
					LOGGER.error("InternalStatusCodeService :: createInternalStatusCodes() : Payment processor {} not found",resourceProcessorCode.getPaymentProcessorId());
					throw new CustomBadRequestException("Payment processor does not exists. Id="+resourceProcessorCode.getPaymentProcessorId());
				}
				PaymentProcessorStatusCode paymentProcessorStatusCode;
				Boolean codeModified = Boolean.FALSE;
				if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
					paymentProcessorStatusCode = paymentProcessorStatusCodeDAO
							.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
									resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
									paymentProcessor);
				} else {
					Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
					paymentProcessorStatusCode = validatePaymentProcessorStatusCode(paymentProcessorCodeId);
					
					codeModified = isCodeModifiedCreateCase(paymentProcessor,paymentProcessorStatusCode,resourceProcessorCode,transactionType.getTransactionTypeName());
				}
				
				LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : paymentProcessorStatusCode value : ", paymentProcessorStatusCode);
				
				paymentProcessorStatusCode = validatePaymentProcessorStatusCode(paymentProcessorStatusCode,internalStatusCodeResource,resourceProcessorCode, codeModified);
				
				paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
				paymentProcessorStatusCode.setPaymentProcessorStatusCodeValue(resourceProcessorCode.getCode());
				paymentProcessorStatusCode.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
				paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());

				// save or update payment processor status code..
				paymentProcessorStatusCode = createOrUpdatePaymentProcessorStatusCode(paymentProcessorStatusCode);
				PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
				// no need to set these two objects  in create case
				LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : paymentProcessorStatusCode  : ",paymentProcessorStatusCode);
				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(paymentProcessorStatusCode.getPaymentProcessorStatusCodeId());
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCode.getInternalStatusCodeId());
				paymentProcessorInternalStatusCode.setLastModifiedBy(currentLoginUserName);
				paymentProcessorInternalStatusCode.setCreatedDate(internalStatusCode.getCreatedDate());
				paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);

			}
			internalStatusCode.getPaymentProcessorInternalStatusCodes().addAll(paymentProcessorInternalStatusCodes);
			LOGGER.debug("No of childs added {}",internalStatusCode.getPaymentProcessorInternalStatusCodes().size());
		} else {
			LOGGER.debug("No payment processor internal status codes found as child items");
		}
		// Finally creating internal status code with parent and child in single transaction
		internalStatusCode = internalStatusCodeDAO.save(internalStatusCode);
		return internalStatusCode;
	}
	
	private PaymentProcessorStatusCode createOrUpdatePaymentProcessorStatusCode(PaymentProcessorStatusCode paymentProcessorStatusCode){
		PaymentProcessorStatusCode paymentProcessorStatusCodeObj;
		if (paymentProcessorStatusCode.getPaymentProcessorStatusCodeId() != null) {
			// update ..
			paymentProcessorStatusCodeObj = paymentProcessorStatusCodeDAO.update(paymentProcessorStatusCode);
		} else {
			paymentProcessorStatusCodeObj = paymentProcessorStatusCodeDAO.save(paymentProcessorStatusCode);
		}
		return paymentProcessorStatusCodeObj;
	}

	private void validateInternalStatusCodeUpdate(InternalStatusCode internalStatusCode,Long internalStatusCodeIdToModify){
		if (internalStatusCode == null) {
			throw new CustomNotFoundException("Internal Status Code does not exist: " + internalStatusCodeIdToModify);
		}
	}
	
	private TransactionType getTransactionType(String transactionTypeName){
		TransactionType transactionType = transactionTypeDAO.findByTransactionType(transactionTypeName);
		
		if (transactionType == null) {
			LOGGER.error("Transaction type {} not found",transactionTypeName);
			throw new CustomBadRequestException("Transaction type="+transactionTypeName+" not exists.");
		}
		return transactionType;
	}
	
	private void validateInternalStatusCode(UpdateInternalCodeResource internalStatusCodeResource,InternalStatusCode internalStatusCode){
		if (!internalStatusCodeResource.getCode().equals(internalStatusCode.getInternalStatusCodeValue())) {
			InternalStatusCode existingInternalResponseCode = internalStatusCodeDAO
					.findByInternalStatusCodeAndTransactionTypeName(internalStatusCodeResource.getCode(),
							internalStatusCodeResource.getTransactionTypeName());
			if (existingInternalResponseCode != null) {
				throw new CustomBadRequestException(
						"Another Internal status code already exists and is assigned to this transaction type.");
			}
		}
	}
	
	private PaymentProcessor validatePaymentProcessor(Long paymentProcessorId){
		PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(paymentProcessorId);
		if (paymentProcessor == null) {
			LOGGER.error("Payment processor {} not found",paymentProcessorId);
			throw new CustomBadRequestException("Payment processor does not exists. Id="+paymentProcessorId);
		}
		return paymentProcessor;
	}
	
	private PaymentProcessorStatusCode validatePaymentProcessorStatusCode(Long paymentProcessorCodeId){
		PaymentProcessorStatusCode paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.findOne(paymentProcessorCodeId);
		if (paymentProcessorStatusCode == null) {
			throw new CustomNotFoundException(
					"Payment Processor Status Code does not exist: " + paymentProcessorCodeId);
		}
		return paymentProcessorStatusCode;
	}
	
	private boolean isCodeModified(PaymentProcessor paymentProcessor,String resourceStatusCodeValue,String paymentProcessorStatusCodeValue,String transactionTypeName){
		if (!resourceStatusCodeValue
				.equals(paymentProcessorStatusCodeValue)) {
			if (paymentProcessorStatusCodeDAO.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
					resourceStatusCodeValue, transactionTypeName,
					paymentProcessor) != null) {
				throw new CustomBadRequestException("The code " + resourceStatusCodeValue	+ " is already used by other Payment Processor Status Code.");
			}
			return true;
		}
		return false;
	}
	
	private void validateCodeOrDesription(PaymentProcessorCodeResource resourceProcessorCode){
		if (resourceProcessorCode.getCode().isEmpty() && resourceProcessorCode.getDescription().isEmpty()) {
			LOGGER.info("Removing payment processor code");
		} else {
			throw new CustomBadRequestException(
					"Unable to save Payment Processor code with code or description empty.");
		}
	}
	
	public InternalStatusCode updateInternalStatusCode(UpdateInternalCodeResource internalStatusCodeResource,String currentLoginUserName) {
		LOGGER.info("InternalStatusCodeService :: updateInternalStatusCode() : Updating InternalStatusCode Record");
		LOGGER.debug("Requested Data= {} , Child Items=",internalStatusCodeResource, internalStatusCodeResource.getPaymentProcessorCodes() != null ? internalStatusCodeResource.getPaymentProcessorCodes().size() : 0 );
		Long internalStatusCodeIdToModify = internalStatusCodeResource.getInternalCodeId();
		LOGGER.debug("Internal Status CodeId to modify {}",internalStatusCodeIdToModify);
		InternalStatusCode internalStatusCode = internalStatusCodeDAO.findOneWithChilds(internalStatusCodeIdToModify);
		validateInternalStatusCodeUpdate(internalStatusCode,internalStatusCodeIdToModify);
		// Get transactionType if null thrown an exception
		TransactionType transactionType = getTransactionType(internalStatusCodeResource.getTransactionTypeName());
		LOGGER.info("Updating internal Status code");

		// Just in case of modify the code of the Internal Status Code, verify
		// if the code is already assigned
		validateInternalStatusCode(internalStatusCodeResource,internalStatusCode);

		internalStatusCode.setInternalStatusCodeValue(internalStatusCodeResource.getCode());
		internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());
		internalStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());
		internalStatusCode.setLastModifiedBy(currentLoginUserName);
		internalStatusCode.setInternalStatusCategory(internalStatusCodeResource.getInternalStatusCategory());
		internalStatusCode.setInternalStatusCategoryAbbr(internalStatusCodeResource.getInternalStatusCategoryAbbr());
		
		Set<Long> paymentProcessorStatusCodeToDelete = new HashSet<>();
		if (internalStatusCodeResource.getPaymentProcessorCodes() != null && !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("Number of payment processor codes to update={}",internalStatusCodeResource.getPaymentProcessorCodes().size());
			// New payment processor Status codes that need to be created or
			// updated
			List<PaymentProcessorStatusCode> newPaymentProcessorStatusCode = new ArrayList<>();

			// New payment processor Status codes that need to be created or
			// updated
			Map<Long, PaymentProcessorStatusCode> newMapOfPaymentProcessorStatusCodes = new HashMap<>();
			
			for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource.getPaymentProcessorCodes()) {

				PaymentProcessor paymentProcessor = validatePaymentProcessor(resourceProcessorCode.getPaymentProcessorId());
				processPaymentProcessorCode(paymentProcessor,resourceProcessorCode,newMapOfPaymentProcessorStatusCodes,internalStatusCode,newPaymentProcessorStatusCode,transactionType.getTransactionTypeName());
				
			}

			// Update information from current payment processor merchants
			updatePaymentProcessorMerchants(internalStatusCode,newMapOfPaymentProcessorStatusCodes,paymentProcessorStatusCodeToDelete);
			
			LOGGER.debug("NewPaymentProcessorStatusCode size : {}",newPaymentProcessorStatusCode.size());
			// Add the new payment processor Status codes
			addNewPaymentProcessorStatusCodes(internalStatusCode,newPaymentProcessorStatusCode,internalStatusCodeIdToModify);
		}
		return internalStatusCodeDAO.update(internalStatusCode);
	}
	
	private void processPaymentProcessorCode(PaymentProcessor paymentProcessor,PaymentProcessorCodeResource resourceProcessorCode,Map<Long, PaymentProcessorStatusCode> newMapOfPaymentProcessorStatusCodes,InternalStatusCode internalStatusCode,List<PaymentProcessorStatusCode> newPaymentProcessorStatusCode,String transactionTypeName){
		if (resourceProcessorCode.getCode() != null && !resourceProcessorCode.getCode().trim().isEmpty() 
				&& resourceProcessorCode.getDescription() != null && !resourceProcessorCode.getDescription().trim().isEmpty()) {
			PaymentProcessorStatusCode paymentProcessorStatusCode;
			Boolean codeModified = false;
			if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
				paymentProcessorStatusCode = paymentProcessorStatusCodeDAO
						.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
								resourceProcessorCode.getCode(), transactionTypeName,
								paymentProcessor);
			} else {
				Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
				paymentProcessorStatusCode = validatePaymentProcessorStatusCode(paymentProcessorCodeId);
				codeModified = isCodeModified(paymentProcessor,resourceProcessorCode.getCode(),paymentProcessorStatusCode.getPaymentProcessorStatusCodeValue(),transactionTypeName);
			}

			LOGGER.debug("PaymentProcessorStatusCode value : {} ",paymentProcessorStatusCode);
			paymentProcessorStatusCode = verifyUpdateChanges(paymentProcessor,paymentProcessorStatusCode,resourceProcessorCode,newPaymentProcessorStatusCode,internalStatusCode,transactionTypeName,codeModified);
			paymentProcessorStatusCode = createOrUpdatePaymentProcessorStatusCode(paymentProcessorStatusCode);
			newMapOfPaymentProcessorStatusCodes.put(paymentProcessorStatusCode.getPaymentProcessorStatusCodeId(),paymentProcessorStatusCode);
		} else {
			validateCodeOrDesription(resourceProcessorCode);
		}
	}
	private void addNewPaymentProcessorStatusCodes(InternalStatusCode internalStatusCode,List<PaymentProcessorStatusCode> newPaymentProcessorStatusCode,Long internalStatusCodeIdToModify){
		for (PaymentProcessorStatusCode current : newPaymentProcessorStatusCode) {
			PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
			paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(current.getPaymentProcessorStatusCodeId());
			paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCodeIdToModify);
			internalStatusCode.getPaymentProcessorInternalStatusCodes().add(paymentProcessorInternalStatusCode);
		}
	}
	
	private void updatePaymentProcessorMerchants(InternalStatusCode internalStatusCode,Map<Long, PaymentProcessorStatusCode> newMapOfPaymentProcessorStatusCodes,Set<Long> paymentProcessorStatusCodeToDelete){
		Iterator<PaymentProcessorInternalStatusCode> iter = internalStatusCode.getPaymentProcessorInternalStatusCodes().iterator();
		while (iter.hasNext()) {
			PaymentProcessorInternalStatusCode element = iter.next();
			PaymentProcessorStatusCode ppmr = newMapOfPaymentProcessorStatusCodes.get(element.getPaymentProcessorStatusCodeId());
			if (ppmr == null) {
				paymentProcessorStatusCodeToDelete.add(element.getPaymentProcessorStatusCodeId());
				iter.remove();
			} else {
				element.setPaymentProcessorStatusCodeId(ppmr.getPaymentProcessorStatusCodeId());
			}
		}
	}

	private PaymentProcessorStatusCode verifyUpdateChanges(PaymentProcessor paymentProcessor,PaymentProcessorStatusCode paymentProcessorStatusCode,PaymentProcessorCodeResource resourceProcessorCode,List<PaymentProcessorStatusCode> newPaymentProcessorStatusCode,InternalStatusCode internalStatusCode,String transactionTypeName,boolean codeModified){
		Set<Long> internalSet = new HashSet<>();
		PaymentProcessorStatusCode paymentProcessorStatusCodeObj;
		if (paymentProcessorStatusCode == null) {
			LOGGER.debug("Creating new payment processor Status code {}", resourceProcessorCode.getCode());
			paymentProcessorStatusCodeObj = new PaymentProcessorStatusCode();
			paymentProcessorStatusCodeObj.setPaymentProcessor(paymentProcessor);
			paymentProcessorStatusCodeObj.setPaymentProcessorStatusCodeValue(resourceProcessorCode.getCode());
			paymentProcessorStatusCodeObj.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
			paymentProcessorStatusCodeObj.setTransactionTypeName(transactionTypeName);

			newPaymentProcessorStatusCode.add(paymentProcessorStatusCodeObj);
		} else {
			paymentProcessorStatusCodeObj = paymentProcessorStatusCode;
			Collection<PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCodeObj
					.getInternalStatusCode();
			if (currentPaymentProcessorInternalStatusCodes != null && !currentPaymentProcessorInternalStatusCodes.isEmpty()) {
				LOGGER.debug("CurrentPaymentProcessorInternalStatusCodes size : {} ",currentPaymentProcessorInternalStatusCodes.size());
				for (PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode : currentPaymentProcessorInternalStatusCodes) {
					validateInternalStatusCode(currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode().getPaymentProcessorStatusCodeValue(),resourceProcessorCode.getCode(),codeModified);
					addInternalStatusCodeIds(currentPaymentProcessorInternalStatusCode,internalSet);
				}
			}
			paymentProcessorStatusCodeObj.setPaymentProcessor(paymentProcessor);
			paymentProcessorStatusCodeObj.setPaymentProcessorStatusCodeValue(resourceProcessorCode.getCode());
			paymentProcessorStatusCodeObj.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
			paymentProcessorStatusCodeObj.setTransactionTypeName(transactionTypeName);
			
			validateInternalStatusCodeId(internalStatusCode,paymentProcessorStatusCodeObj,internalSet,newPaymentProcessorStatusCode);
			
		}
		return paymentProcessorStatusCodeObj;
	}
	
	private void validateInternalStatusCodeId(InternalStatusCode internalStatusCode,PaymentProcessorStatusCode paymentProcessorStatusCodeObj,Set<Long> internalSet,List<PaymentProcessorStatusCode> newPaymentProcessorStatusCode){
		if (!internalSet.contains(internalStatusCode.getInternalStatusCodeId())) {
			newPaymentProcessorStatusCode.add(paymentProcessorStatusCodeObj);
		}
	}
	
	private void addInternalStatusCodeIds(PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode,Set<Long> internalSet){
		Long internalStatusCodeId = currentPaymentProcessorInternalStatusCode
				.getInternalStatusCode() != null ? currentPaymentProcessorInternalStatusCode
						.getInternalStatusCode().getInternalStatusCodeId() : null;
        if (internalStatusCodeId != null) {
			internalSet.add(internalStatusCodeId);
		}
	}
	
	public void deleteInternalStatusCode(Long internalStatusCodeId) {
		InternalStatusCode internalStatusCodeToDelete = internalStatusCodeDAO.findOne(internalStatusCodeId);

		if (internalStatusCodeToDelete == null) {
			throw new CustomNotFoundException(String.format("Unable to find internal Status code with id = [%s]", internalStatusCodeId));
		}
		// need to find all payment processor status code ids which used by payment processor internal status code of requested internalStatusCodeId to delete
		List<Long> paymentProcessorStatusCodeIds = paymentProcessorInternalStatusCodeDAO.findPaymentProcessorStatusCodeIdsForInternalStatusCodeId(internalStatusCodeId);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("deleteInternalStatusCode() : paymentProcessorStatusCodeIds size : {}",paymentProcessorStatusCodeIds.size());
		}
		// First delete internal status code and payment processor internal status code
		internalStatusCodeDAO.delete(internalStatusCodeId);
		// Second delete all payment processor status code which were in used by deleted internal status code
		if(paymentProcessorStatusCodeIds != null){
			paymentProcessorInternalStatusCodeDAO.deletePaymentProcessorStatusCodeIds(paymentProcessorStatusCodeIds);
		}
		
	}

	public InternalStatusCode getInternalStatusCode(Long internalStatusCodeId) {
		InternalStatusCode internalStatusCode = internalStatusCodeDAO.findOne(internalStatusCodeId);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getInternalStatusCode() : internalStatusCode : {} ",internalStatusCode);
		}
		if (internalStatusCode != null) {
			List<PaymentProcessorInternalStatusCode> list = paymentProcessorInternalStatusCodeDAO.findAllForInternalStatusCodeId(internalStatusCodeId);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("getInternalStatusCode() : PaymentProcessorInternalStatusCode size : ",list.size());
			}
			if (list != null) {
				internalStatusCode.setPaymentProcessorInternalStatusCodes(list);
			}
		}
		return internalStatusCode;
	}
	
	public String getLetterFromStatusCodeForSaleTransactions(String statusCode) {
		InternalStatusCode internalStatusCode = internalStatusCodeDAO
				.findByInternalStatusCodeAndTransactionTypeName(statusCode, "SALE");
		LOGGER.debug("nternalStatusCodeService :: getLetterFromStatusCodeForSaleTransactions() : internalStatusCode : "+internalStatusCode);
		if (internalStatusCode == null)
			return "";
		else
			return internalStatusCode.getInternalStatusCategoryAbbr();
	}
}
