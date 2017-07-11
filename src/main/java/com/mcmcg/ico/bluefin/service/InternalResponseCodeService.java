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

import com.mcmcg.ico.bluefin.model.InternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.repository.InternalResponseCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorInternalResponseCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorResponseCodeDAO;
import com.mcmcg.ico.bluefin.repository.TransactionTypeDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateInternalCodeResource;

@Service
@Transactional
public class InternalResponseCodeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalResponseCodeService.class);

	@Autowired
	private InternalResponseCodeDAO internalResponseCodeDAO;
	
	@Autowired
	private PaymentProcessorInternalResponseCodeDAO paymentProcessorInternalResponseCodeDAO;
	@Autowired
	private PaymentProcessorResponseCodeDAO paymentProcessorResponseCodeDAO;
	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;
	@Autowired
	private TransactionTypeDAO transactionTypeDAO;

	public List<InternalResponseCode> getInternalResponseCodesByTransactionType(String transactionType) {
		// Get transactionType if null thrown an exception
	
		List<InternalResponseCode> internalResponseCodeList =internalResponseCodeDAO.findByTransactionTypeNameOrderByInternalResponseCodeAsc(transactionType);
		LOGGER.debug("internalResponseCodeList size is ={} ",internalResponseCodeList.size());
		if(null != internalResponseCodeList && !internalResponseCodeList.isEmpty()){
			for (InternalResponseCode internalResponseCode : internalResponseCodeList) {
				internalResponseCode.setPaymentProcessorInternalResponseCodes(paymentProcessorInternalResponseCodeDAO.findPaymentProcessorInternalResponseCodeListByInternalResponseCodeId(internalResponseCode.getInternalResponseCodeId()));
				for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : internalResponseCode.getPaymentProcessorInternalResponseCodes()){
					Long paymentProcessorResponseCodeId = paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId();
					PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.findOne(paymentProcessorResponseCodeId);
					paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
					paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
				}
			}
		}
		return internalResponseCodeList;
	}

	private void validateInternalResponseCode(InternalResponseCode internalResponseCode){
		if (internalResponseCode != null) {
			throw new CustomBadRequestException(
					"Internal response code already exists and is assigned to this transaction type.");
		}
	}
	
	private void validateInternalResponseCodeUpdate(InternalResponseCode internalResponseCode,Long internalResponseCodeIdToModify){
		if (internalResponseCode == null) {
			throw new CustomNotFoundException("Internal Response Code does not exist: " + internalResponseCodeIdToModify);
		}
	}
	
	private void validateInternalResponseCode(String currentResponseCode,String resourceProcessorCode,boolean codeModified){
		if (!currentResponseCode.equals(resourceProcessorCode)	&& !codeModified) {
			throw new CustomBadRequestException(
					"This Payment Processor is already related to another Internal Response Code.");
		}
	}
	
	private InternalResponseCode populateInternalResponseCode(String code,String desc,String transactionType,String userName){
		InternalResponseCode internalResponseCode = new InternalResponseCode();
		internalResponseCode.setInternalResponseCodeValue(code);
		internalResponseCode.setInternalResponseCodeDescription(desc);
		internalResponseCode.setLastModifiedBy(userName);
		internalResponseCode.setTransactionTypeName(transactionType);
		internalResponseCode
				.setPaymentProcessorInternalResponseCodes(new ArrayList<PaymentProcessorInternalResponseCode>());
		return internalResponseCode;
	}
	
	private PaymentProcessorResponseCode createOrUpdatePaymentProcessorResponseCode(PaymentProcessorResponseCode paymentProcessorResponseCode,InternalCodeResource internalResponseCodeResource,String code,boolean codeModified){
		PaymentProcessorResponseCode paymentProcessorResponseCodeObj;
		if (paymentProcessorResponseCode == null) {
			LOGGER.debug("Creating new payment processor response code {}", code);
			paymentProcessorResponseCodeObj = new PaymentProcessorResponseCode();
		} else {
			Collection<PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
					.getInternalResponseCode();
			LOGGER.debug("currentPaymentProcessorInternalResponseCodes size : ",currentPaymentProcessorInternalResponseCodes.size() );
			for (PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
				validatePaymentProcessorResponseCode(currentPaymentProcessorInternalResponseCode,internalResponseCodeResource,codeModified);
			}
			paymentProcessorResponseCodeObj = paymentProcessorResponseCode;
		}
		return paymentProcessorResponseCodeObj;
	}
	
	public InternalResponseCode createInternalResponseCodes(InternalCodeResource internalResponseCodeResource,String userName) {

		// Get transactionType if null thrown an exception
		TransactionType transactionType = transactionTypeDAO
				.findByTransactionType(internalResponseCodeResource.getTransactionTypeName());

		InternalResponseCode internalResponseCode = internalResponseCodeDAO
				.findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
						transactionType.getTransactionTypeName());

		validateInternalResponseCode(internalResponseCode);
		internalResponseCode = populateInternalResponseCode(internalResponseCodeResource.getCode(),internalResponseCodeResource.getDescription(),transactionType.getTransactionTypeName(),userName);
		LOGGER.debug("Creating new internal response code {}", internalResponseCodeResource.getCode());
		
		if (internalResponseCodeResource.getPaymentProcessorCodes() != null && !internalResponseCodeResource.getPaymentProcessorCodes().isEmpty()) {
			List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = new ArrayList<>();
			for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource.getPaymentProcessorCodes()) {

				PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());

				PaymentProcessorResponseCode paymentProcessorResponseCode;
				Boolean codeModified = Boolean.FALSE;
				if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
					paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
							.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
									resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
									paymentProcessor);
				} else {
					Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
					paymentProcessorResponseCode = validatePaymentProcessorResponseCode(paymentProcessorCodeId);
					codeModified = isCodeModified(paymentProcessor,resourceProcessorCode.getCode(),paymentProcessorResponseCode.getPaymentProcessorResponseCodeValue(),transactionType.getTransactionTypeName());
				}
				
				LOGGER.debug("paymentProcessorResponseCode value : ",paymentProcessorResponseCode);
				paymentProcessorResponseCode = createOrUpdatePaymentProcessorResponseCode(paymentProcessorResponseCode,internalResponseCodeResource,resourceProcessorCode.getCode(),codeModified);

				paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
				paymentProcessorResponseCode.setPaymentProcessorResponseCodeValue(resourceProcessorCode.getCode());
				paymentProcessorResponseCode
						.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
				paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

				// save or update payment processor status code..
				paymentProcessorResponseCode = saveOrUpdatePaymentProcessorResponseCode(paymentProcessorResponseCode);
				
				PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new PaymentProcessorInternalResponseCode();
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
				paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
				paymentProcessorInternalResponseCodes.add(paymentProcessorInternalResponseCode);

			}
			if (internalResponseCode.getPaymentProcessorInternalResponseCodes() != null) {
				internalResponseCode.getPaymentProcessorInternalResponseCodes().clear();
			}
			internalResponseCode.getPaymentProcessorInternalResponseCodes()
					.addAll(paymentProcessorInternalResponseCodes);

		}
		LOGGER.info("Ready to save internalResponseCode");
		return internalResponseCodeDAO.save(internalResponseCode);

	}
	
	private void validatePaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(PaymentProcessor paymentProcessor,String code,String transactionTypeName){
		if (paymentProcessorResponseCodeDAO.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
						code, transactionTypeName,
						paymentProcessor) != null) {
			throw new CustomBadRequestException("The code " + code
					+ " is already used by other Payment Processor Response Code.");
		}
	}
	
	private void validatePaymentProcessorResponseCode(PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode,InternalCodeResource internalResponseCodeResource,boolean codeModified){
		if (currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode() != null && 
				! StringUtils.equals(currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeValue(),
				internalResponseCodeResource.getCode()) && !codeModified) {
			throw new CustomBadRequestException(
					"This Payment Processor is already related to another Internal Response Code.");
		}
	}
	
	private PaymentProcessorResponseCode saveOrUpdatePaymentProcessorResponseCode(PaymentProcessorResponseCode paymentProcessorResponseCode){
		PaymentProcessorResponseCode paymentProcessorResponseCodeObj;
		if (paymentProcessorResponseCode.getPaymentProcessorResponseCodeId() != null) {
			// update ..
			paymentProcessorResponseCodeObj = paymentProcessorResponseCodeDAO.update(paymentProcessorResponseCode);
		} else {
			paymentProcessorResponseCodeObj = paymentProcessorResponseCodeDAO
					.save(paymentProcessorResponseCode);
		}
		return paymentProcessorResponseCodeObj;
	}

	private TransactionType getTransactionType(String transactionTypeName){
		TransactionType transactionType = transactionTypeDAO.findByTransactionType(transactionTypeName);
		
		if (transactionType == null) {
			LOGGER.error("Transaction type {} not found",transactionTypeName);
			throw new CustomBadRequestException("Transaction type="+transactionTypeName+" not exists.");
		}
		return transactionType;
	}
	
	private void validateInternalResponseCode(UpdateInternalCodeResource internalResponseCodeResource,InternalResponseCode internalResponseCode){
		if (!internalResponseCodeResource.getCode().equals(internalResponseCode.getInternalResponseCodeValue())) {
			InternalResponseCode existingInternalResponseCode = internalResponseCodeDAO
					.findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
							internalResponseCodeResource.getTransactionTypeName());
			if (existingInternalResponseCode != null) {
				throw new CustomBadRequestException(
						"Another Internal response code already exists and is assigned to this transaction type.");
			}
		}
	}
	
	private void addInternalResponseCodeIds(PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode,Set<Long> internalSet){
		Long internalResponseCodeId = currentPaymentProcessorInternalResponseCode
				.getInternalResponseCode() != null ? currentPaymentProcessorInternalResponseCode
						.getInternalResponseCode().getInternalResponseCodeId() : null;
		if (internalResponseCodeId != null) {
				internalSet.add(internalResponseCodeId);
		}
	}
	
	private PaymentProcessorResponseCode createOrUpdatePaymentProcessorResponseCode(PaymentProcessorResponseCode paymentProcessorResponseCode){
		if (paymentProcessorResponseCode != null) {
			PaymentProcessorResponseCode paymentProcessorResponseCodeObj;
			if (paymentProcessorResponseCode.getPaymentProcessorResponseCodeId() != null) {
				// update payment processor status code
				paymentProcessorResponseCodeObj = paymentProcessorResponseCodeDAO.update(paymentProcessorResponseCode);
			} else {
				paymentProcessorResponseCodeObj = paymentProcessorResponseCodeDAO.save(paymentProcessorResponseCode);
			} 
			return paymentProcessorResponseCodeObj;
		}
		return null;
	}
	
	private PaymentProcessor validatePaymentProcessor(Long paymentProcessorId){
		PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(paymentProcessorId);
		if (paymentProcessor == null) {
			LOGGER.error("Payment processor {} not found",paymentProcessorId);
			throw new CustomBadRequestException("Payment processor does not exists. Id="+paymentProcessorId);
		}
		return paymentProcessor;
	}
	
	private PaymentProcessorResponseCode validatePaymentProcessorResponseCode(Long paymentProcessorCodeId){
		PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.findOne(paymentProcessorCodeId);
		if (paymentProcessorResponseCode == null) {
			throw new CustomNotFoundException(
					"Payment Processor Response Code does not exist: " + paymentProcessorCodeId);
		}
		return paymentProcessorResponseCode;
	}
	
	
	private boolean isCodeModified(PaymentProcessor paymentProcessor,String resourceProcessorCodeValue,String paymentProcessorResponseCodeValue,String transactionTypeName){
		if (!resourceProcessorCodeValue
				.equals(paymentProcessorResponseCodeValue)) {
			validatePaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(paymentProcessor,
					resourceProcessorCodeValue, transactionTypeName);
			return true;
		}
		return false;
	}
	
	private PaymentProcessorResponseCode verifyUpdateChanges(PaymentProcessor paymentProcessor,PaymentProcessorResponseCode paymentProcessorResponseCode,InternalResponseCode internalResponseCode,PaymentProcessorCodeResource resourceProcessorCode,
			List<PaymentProcessorResponseCode> newPaymentProcessorResponseCode,String transactionTypeName,boolean codeModified ){
		Set<Long> internalSet = new HashSet<>();
		PaymentProcessorResponseCode paymentProcessorResponseCodeObj;
		if (paymentProcessorResponseCode == null) {
			LOGGER.debug("Creating new payment processor response code {}", resourceProcessorCode.getCode());
			paymentProcessorResponseCodeObj = new PaymentProcessorResponseCode();

			paymentProcessorResponseCodeObj.setPaymentProcessor(paymentProcessor);
			paymentProcessorResponseCodeObj.setPaymentProcessorResponseCodeValue(resourceProcessorCode.getCode());
			paymentProcessorResponseCodeObj.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
			paymentProcessorResponseCodeObj.setTransactionTypeName(transactionTypeName);

			newPaymentProcessorResponseCode.add(paymentProcessorResponseCodeObj);
		} else {
			paymentProcessorResponseCodeObj = paymentProcessorResponseCode;
			Collection<PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCodeObj
					.getInternalResponseCode();
			LOGGER.debug("CurrentPaymentProcessorInternalResponseCodes size : {}",currentPaymentProcessorInternalResponseCodes != null ? currentPaymentProcessorInternalResponseCodes.size() : 0 );
			if (currentPaymentProcessorInternalResponseCodes != null && !currentPaymentProcessorInternalResponseCodes.isEmpty()) {
				for (PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
					
					validateInternalResponseCode(currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeValue(),resourceProcessorCode.getCode(),codeModified);
					
					addInternalResponseCodeIds(currentPaymentProcessorInternalResponseCode,internalSet);
				}
			}
			paymentProcessorResponseCodeObj.setPaymentProcessor(paymentProcessor);
			paymentProcessorResponseCodeObj.setPaymentProcessorResponseCodeValue(resourceProcessorCode.getCode());
			paymentProcessorResponseCodeObj
					.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
			paymentProcessorResponseCodeObj.setTransactionTypeName(transactionTypeName);
			
			validateInternalResponseCodeId(internalResponseCode.getInternalResponseCodeId(),paymentProcessorResponseCodeObj,internalSet,newPaymentProcessorResponseCode);
		}
		return paymentProcessorResponseCodeObj;
	}
	
	private void validateInternalResponseCodeId(Long internalResponseCodeId,PaymentProcessorResponseCode paymentProcessorResponseCodeObj,Set<Long> internalSet,List<PaymentProcessorResponseCode> newPaymentProcessorResponseCode){
		if (!internalSet.contains(internalResponseCodeId)) {
			newPaymentProcessorResponseCode.add(paymentProcessorResponseCodeObj);
		}
	}
	public InternalResponseCode updateInternalResponseCode(UpdateInternalCodeResource internalResponseCodeResource) {
		LOGGER.debug("Updating InternalResponseCode Record, Requested Data= {} , Child Items= {}",internalResponseCodeResource ,internalResponseCodeResource.getPaymentProcessorCodes() != null ? internalResponseCodeResource.getPaymentProcessorCodes().size() : 0 );
		Long internalResponseCodeIdToModify = internalResponseCodeResource.getInternalCodeId();
		LOGGER.debug("Internal Response CodeId to modify {}",internalResponseCodeIdToModify);
		
		InternalResponseCode internalResponseCode = internalResponseCodeDAO.findOneWithChilds(internalResponseCodeIdToModify);
		
		validateInternalResponseCodeUpdate(internalResponseCode,internalResponseCodeIdToModify);

		// Get transactionType if null thrown an exception
		TransactionType transactionType = getTransactionType(internalResponseCodeResource.getTransactionTypeName());
		LOGGER.debug("Updating internal response code {}", internalResponseCodeIdToModify);

		// Just in case of modify the code of the Internal Response Code, verify
		// if the code is already assigned
		validateInternalResponseCode(internalResponseCodeResource,internalResponseCode);

		internalResponseCode.setInternalResponseCodeValue(internalResponseCodeResource.getCode());
		internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
		internalResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

		Set<Long> paymentProcessorResponseCodeToDelete = new HashSet<>();
		if (internalResponseCodeResource.getPaymentProcessorCodes() != null	&& !internalResponseCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("Number of payment processor codes to update= {}",internalResponseCodeResource.getPaymentProcessorCodes().size());
			// New payment processor response codes that need to be created or
			// updated
			List<PaymentProcessorResponseCode> newPaymentProcessorResponseCode = new ArrayList<>();

			// New payment processor response codes that need to be created or
			// updated
			Map<Long, PaymentProcessorResponseCode> newMapOfPaymentProcessorResponseCodes = new HashMap<>();

			for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource.getPaymentProcessorCodes()) {

				PaymentProcessor paymentProcessor = validatePaymentProcessor(resourceProcessorCode.getPaymentProcessorId());
				
				if (!resourceProcessorCode.getCode().isEmpty() && !resourceProcessorCode.getDescription().isEmpty()) {
					processResponseCode(paymentProcessor, resourceProcessorCode,internalResponseCode, newPaymentProcessorResponseCode, newMapOfPaymentProcessorResponseCodes,transactionType.getTransactionTypeName());
				} else {
					validateCodeOrDesription(resourceProcessorCode);
				}
			}

			// Update information from current payment processor merchants
			updateInformationForCurrentProcessorMerchants(internalResponseCode,newMapOfPaymentProcessorResponseCodes,paymentProcessorResponseCodeToDelete);

			LOGGER.info("newPaymentProcessorResponseCode size : ", newPaymentProcessorResponseCode.size()); 
			// Add the new payment processor response codes
			addNewPaymentProcessorResponseCodes(internalResponseCode,internalResponseCodeIdToModify,newPaymentProcessorResponseCode);

		}
		return internalResponseCodeDAO.update(internalResponseCode);
	}

	private void processResponseCode(PaymentProcessor paymentProcessor, PaymentProcessorCodeResource resourceProcessorCode,InternalResponseCode internalResponseCode,List<PaymentProcessorResponseCode> newPaymentProcessorResponseCode,Map<Long, PaymentProcessorResponseCode> newMapOfPaymentProcessorResponseCodes,String transactionTypeName){
		Boolean codeModified = false;
		PaymentProcessorResponseCode paymentProcessorResponseCode;
		if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
			paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
					.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
							resourceProcessorCode.getCode(), transactionTypeName,
							paymentProcessor);
		} else {
			Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
			paymentProcessorResponseCode = validatePaymentProcessorResponseCode(paymentProcessorCodeId);
			codeModified = isCodeModified(paymentProcessor,resourceProcessorCode.getCode(),paymentProcessorResponseCode.getPaymentProcessorResponseCodeValue(),transactionTypeName);
		}

		LOGGER.debug("PaymentProcessorResponseCode value : {}",paymentProcessorResponseCode);
		paymentProcessorResponseCode = verifyUpdateChanges(paymentProcessor,paymentProcessorResponseCode,internalResponseCode,resourceProcessorCode,
				 newPaymentProcessorResponseCode,transactionTypeName, codeModified );
		// update payment processor status code
		paymentProcessorResponseCode = createOrUpdatePaymentProcessorResponseCode(paymentProcessorResponseCode);
		checkIfPutPaymentProcessorResponseCodeToUpdate(paymentProcessorResponseCode,newMapOfPaymentProcessorResponseCodes);
	}
	
	private void checkIfPutPaymentProcessorResponseCodeToUpdate(PaymentProcessorResponseCode paymentProcessorResponseCode,Map<Long, PaymentProcessorResponseCode> newMapOfPaymentProcessorResponseCodes){
		if (paymentProcessorResponseCode != null) {
			newMapOfPaymentProcessorResponseCodes.put(paymentProcessorResponseCode.getPaymentProcessorResponseCodeId(),paymentProcessorResponseCode);
		}
	}
	private void validateCodeOrDesription(PaymentProcessorCodeResource resourceProcessorCode){
		if (resourceProcessorCode.getCode().isEmpty() && resourceProcessorCode.getDescription().isEmpty()) {
			LOGGER.info("Removing payment processor code");
		} else {
			throw new CustomBadRequestException(
					"Unable to save Payment Processor code with code or description empty.");
		}
	}
	
	private void addNewPaymentProcessorResponseCodes(InternalResponseCode internalResponseCode,Long internalResponseCodeIdToModify,List<PaymentProcessorResponseCode> newPaymentProcessorResponseCode){
		for (PaymentProcessorResponseCode current : newPaymentProcessorResponseCode) {
			PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new PaymentProcessorInternalResponseCode();
			paymentProcessorInternalResponseCode.setPaymentProcessorResponseCodeId(current.getPaymentProcessorResponseCodeId());
			paymentProcessorInternalResponseCode.setInternalResponseCodeId(internalResponseCodeIdToModify);
			internalResponseCode.getPaymentProcessorInternalResponseCodes().add(paymentProcessorInternalResponseCode);
		}
	}
	private void updateInformationForCurrentProcessorMerchants(InternalResponseCode internalResponseCode,Map<Long, PaymentProcessorResponseCode> newMapOfPaymentProcessorResponseCodes,Set<Long> paymentProcessorResponseCodeToDelete){
		Iterator<PaymentProcessorInternalResponseCode> iter = internalResponseCode
				.getPaymentProcessorInternalResponseCodes().iterator();
		while (iter.hasNext()) {
			PaymentProcessorInternalResponseCode element = iter.next();

			PaymentProcessorResponseCode ppmr = newMapOfPaymentProcessorResponseCodes
					.get(element.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
			if (ppmr == null) {
				paymentProcessorResponseCodeToDelete
						.add(element.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
				iter.remove();
			} else {
				element.setPaymentProcessorResponseCode(ppmr);
			}
		}
	}
	public void deleteInternalResponseCode(Long id) {
		InternalResponseCode internalResponseCodeToDelete = internalResponseCodeDAO.findOne(id);

		if (internalResponseCodeToDelete == null) {
			throw new CustomNotFoundException(
					String.format("Unable to find internal response code with id = [%s]", id));
		}
		List<Long > paymentProcessorResponseCodeIds = paymentProcessorInternalResponseCodeDAO.findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode(id);
		LOGGER.info("paymentProcessorResponseCodeIds size : {} ", paymentProcessorResponseCodeIds != null ? paymentProcessorResponseCodeIds.size() : 0);
		internalResponseCodeDAO.delete(internalResponseCodeToDelete);
		if(paymentProcessorResponseCodeIds != null && !paymentProcessorResponseCodeIds.isEmpty()){
			paymentProcessorInternalResponseCodeDAO.deletePaymentProcessorResponseCodeIds(paymentProcessorResponseCodeIds);
		}
		
	}
}
