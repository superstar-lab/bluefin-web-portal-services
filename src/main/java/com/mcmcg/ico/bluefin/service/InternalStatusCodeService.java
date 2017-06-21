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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.InternalStatusCode;
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

	public List<com.mcmcg.ico.bluefin.model.InternalStatusCode> getInternalStatusCodesByTransactionType(final String transactionType) {
		List<com.mcmcg.ico.bluefin.model.InternalStatusCode> internalStatusCodeList = internalStatusCodeDAO.findByTransactionTypeNameOrderByInternalStatusCodeAsc(transactionType);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("InternalStatusCodeService :: getInternalStatusCodesByTransactionType() : internalStatusCodeList size : {}",internalStatusCodeList.size());
		}
		if (internalStatusCodeList != null) {
			for(com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode : internalStatusCodeList){
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
	
	public com.mcmcg.ico.bluefin.model.InternalStatusCode createInternalStatusCodes(InternalCodeResource internalStatusCodeResource,String currentLoginUserName) {

		// Get transactionType if null thrown an exception
		TransactionType transactionType = transactionTypeDAO
				.findByTransactionType(internalStatusCodeResource.getTransactionTypeName());
		if (transactionType == null) {
			LOGGER.error("InternalStatusCodeService :: createInternalStatusCodes() : Transaction type {} not found",internalStatusCodeResource.getTransactionTypeName());
			throw new CustomBadRequestException("Transaction type not exists.");
		}
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO
				.findByInternalStatusCodeAndTransactionTypeName(internalStatusCodeResource.getCode(),
						transactionType.getTransactionTypeName());

		if (internalStatusCode != null) {
			throw new CustomBadRequestException(
					"Internal Status code already exists and is assigned to this transaction type.");
		}

		LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Creating new internal Status code {}", internalStatusCodeResource.getCode());
		internalStatusCode = new com.mcmcg.ico.bluefin.model.InternalStatusCode();
		internalStatusCode.setInternalStatusCode(internalStatusCodeResource.getCode());
		internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());
		internalStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());
		internalStatusCode.setPaymentProcessorInternalStatusCodes(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode>());
		internalStatusCode.setInternalStatusCategory(internalStatusCodeResource.getInternalStatusCategory());
		internalStatusCode.setInternalStatusCategoryAbbr(internalStatusCodeResource.getInternalStatusCategoryAbbr());
		internalStatusCode.setLastModifiedBy(currentLoginUserName);
		
		if ( internalStatusCodeResource.getPaymentProcessorCodes() != null && !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Number of payment processor internal status codes="+internalStatusCodeResource.getPaymentProcessorCodes().size());
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = new ArrayList<>();
			for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource.getPaymentProcessorCodes()) {
				LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Payment processor internal status code="+resourceProcessorCode);
				// validate if payment processor is exists or not
				com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());
				if (paymentProcessor == null) {
					LOGGER.error("InternalStatusCodeService :: createInternalStatusCodes() : Payment processor {} not found",resourceProcessorCode.getPaymentProcessorId());
					throw new CustomBadRequestException("Payment processor does not exists. Id="+resourceProcessorCode.getPaymentProcessorId());
				}
				com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCode;
				Boolean codeModified = Boolean.FALSE;
				if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
					paymentProcessorStatusCode = paymentProcessorStatusCodeDAO
							.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
									resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
									paymentProcessor);
				} else {
					Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
					paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.findOne(paymentProcessorCodeId);
					if (paymentProcessorStatusCode == null) {
						throw new CustomNotFoundException("Payment Processor Status Code does not exist: " + paymentProcessorCodeId);
					} else if (!paymentProcessorStatusCode.getPaymentProcessorStatusCode().equals(resourceProcessorCode.getCode())) {
						codeModified = true;
						if (paymentProcessorStatusCodeDAO
								.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
										resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
										paymentProcessor) != null) {
							throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode() + " is already used by other Payment Processor Status Code.");
						}
					}
				}
				
				LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : paymentProcessorStatusCode value : ", paymentProcessorStatusCode);
				if (paymentProcessorStatusCode == null) {
					LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : Creating new payment processor Status code {}", resourceProcessorCode.getCode());
					paymentProcessorStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode();
				} else {
					Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
							.getInternalStatusCode();
					LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : PaymentProcessorInternalStatusCode size : {} ", currentPaymentProcessorInternalStatusCodes.size());
					if ( currentPaymentProcessorInternalStatusCodes != null && !currentPaymentProcessorInternalStatusCodes.isEmpty()) {
						for (com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode : currentPaymentProcessorInternalStatusCodes) {
							if (!currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode()
								.equals(internalStatusCodeResource.getCode()) && !codeModified) {
								throw new CustomBadRequestException(
									"This Payment Processor is already related to another Internal Status Code.");
							}
						}
					}
				}

				paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
				paymentProcessorStatusCode.setPaymentProcessorStatusCode(resourceProcessorCode.getCode());
				paymentProcessorStatusCode.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
				paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());

				// save or update payment processor status code..
				if (paymentProcessorStatusCode.getPaymentProcessorStatusCodeId() != null) {
					// update ..
					paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.update(paymentProcessorStatusCode);
				} else {
					paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.save(paymentProcessorStatusCode);
				}
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode();
				// no need to set these two objects  in create case
				LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : paymentProcessorStatusCode  : ",paymentProcessorStatusCode);
				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(paymentProcessorStatusCode.getPaymentProcessorStatusCodeId());
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCode.getInternalStatusCodeId());
				paymentProcessorInternalStatusCode.setLastModifiedBy(currentLoginUserName);
				paymentProcessorInternalStatusCode.setCreatedDate(internalStatusCode.getCreatedDate());
				paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);

			}
			internalStatusCode.getPaymentProcessorInternalStatusCodes().addAll(paymentProcessorInternalStatusCodes);
			LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : No of childs added "+internalStatusCode.getPaymentProcessorInternalStatusCodes().size());
		} else {
			LOGGER.debug("InternalStatusCodeService :: createInternalStatusCodes() : No payment processor internal status codes found as child items");
		}
		// Finally creating internal status code with parent and child in single transaction
		internalStatusCode = internalStatusCodeDAO.save(internalStatusCode);
		return internalStatusCode;
	}

	public com.mcmcg.ico.bluefin.model.InternalStatusCode updateInternalStatusCode(UpdateInternalCodeResource internalStatusCodeResource,String currentLoginUserName) {
		LOGGER.info("InternalStatusCodeService :: updateInternalStatusCode() : Updating InternalStatusCode Record");
		LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : Requested Data="+(internalStatusCodeResource) + " , Child Items="+ ( internalStatusCodeResource.getPaymentProcessorCodes() != null ? internalStatusCodeResource.getPaymentProcessorCodes().size() : 0 ) );
		Long internalStatusCodeIdToModify = internalStatusCodeResource.getInternalCodeId();
		LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() :  Internal Status CodeId to modify {}"+internalStatusCodeIdToModify);
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO.findOneWithChilds(internalStatusCodeIdToModify);
		if (internalStatusCode == null) {
			throw new CustomNotFoundException("Internal Status Code does not exist: " + internalStatusCodeIdToModify);
		}
		// Get transactionType if null thrown an exception
		TransactionType transactionType = transactionTypeDAO.findByTransactionType(internalStatusCodeResource.getTransactionTypeName());
		if (transactionType == null) {
			LOGGER.error("InternalStatusCodeService :: updateInternalStatusCode() : Transaction type {} not found",internalStatusCodeResource.getTransactionTypeName());
			throw new CustomBadRequestException("Transaction type "+internalStatusCodeResource.getTransactionTypeName()+" not exists.");
		}
		LOGGER.info("InternalStatusCodeService :: updateInternalStatusCode() : Updating internal Status code");

		// Just in case of modify the code of the Internal Status Code, verify
		// if the code is already assigned
		if (!internalStatusCodeResource.getCode().equals(internalStatusCode.getInternalStatusCodeValue())) {
			com.mcmcg.ico.bluefin.model.InternalStatusCode existingInternalStatusCode = internalStatusCodeDAO
					.findByInternalStatusCodeAndTransactionTypeName(internalStatusCodeResource.getCode(),
							transactionType.getTransactionTypeName());
			if (existingInternalStatusCode != null) {
				throw new CustomBadRequestException(
						"Another Internal Status code already exists and is assigned to this transaction type.");
			}
		}

		internalStatusCode.setInternalStatusCode(internalStatusCodeResource.getCode());
		internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());
		internalStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());
		internalStatusCode.setLastModifiedBy(currentLoginUserName);
		internalStatusCode.setInternalStatusCategory(internalStatusCodeResource.getInternalStatusCategory());
		internalStatusCode.setInternalStatusCategoryAbbr(internalStatusCodeResource.getInternalStatusCategoryAbbr());
		
		Set<Long> paymentProcessorStatusCodeToDelete = new HashSet<>();
		if (internalStatusCodeResource.getPaymentProcessorCodes() != null && !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : Number of payment processor codes to update="+internalStatusCodeResource.getPaymentProcessorCodes().size());
			// New payment processor Status codes that need to be created or
			// updated
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> newPaymentProcessorStatusCode = new ArrayList<>();

			// New payment processor Status codes that need to be created or
			// updated
			Map<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> newMapOfPaymentProcessorStatusCodes = new HashMap<>();

			LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : internalStatusCodeResource size : "
					+(internalStatusCodeResource == null ? null : (internalStatusCodeResource.getPaymentProcessorCodes() == null ? null : internalStatusCodeResource.getPaymentProcessorCodes().size())));
			for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource.getPaymentProcessorCodes()) {

				com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());
				if (paymentProcessor == null) {
					LOGGER.error("InternalStatusCodeService :: updateInternalStatusCode() : Payment processor {} not found",resourceProcessorCode.getPaymentProcessorId());
					throw new CustomBadRequestException("Payment processor does not exists. Id="+resourceProcessorCode.getPaymentProcessorId());
				}
				if (resourceProcessorCode.getCode() != null && !resourceProcessorCode.getCode().trim().isEmpty() 
						&& resourceProcessorCode.getDescription() != null && !resourceProcessorCode.getDescription().trim().isEmpty()) {
					com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCode;
					Boolean codeModified = false;
					if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
						paymentProcessorStatusCode = paymentProcessorStatusCodeDAO
								.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
										resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
										paymentProcessor);
					} else {
						Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
						paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.findOne(paymentProcessorCodeId);
						if (paymentProcessorStatusCode == null) {
							throw new CustomNotFoundException(
									"Payment Processor Status Code does not exist: " + paymentProcessorCodeId);
						} else if (!paymentProcessorStatusCode.getPaymentProcessorStatusCode().equals(resourceProcessorCode.getCode())) {
							codeModified = true;
							if (paymentProcessorStatusCodeDAO.findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
											resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
											paymentProcessor) != null) {
								throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()	+ " is already used by other Payment Processor Status Code.");
							}
						}
					}

					LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : paymentProcessorStatusCode value : "+paymentProcessorStatusCode);
					Set<Long> internalSet = new HashSet<>();
					if (paymentProcessorStatusCode == null) {
						LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : Creating new payment processor Status code {}", resourceProcessorCode.getCode());
						paymentProcessorStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode();
						paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorStatusCode.setPaymentProcessorStatusCode(resourceProcessorCode.getCode());
						paymentProcessorStatusCode.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());

						newPaymentProcessorStatusCode.add(paymentProcessorStatusCode);

					} else {
						Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
								.getInternalStatusCode();
						LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : currentPaymentProcessorInternalStatusCodes size : "+currentPaymentProcessorInternalStatusCodes.size());
						if (currentPaymentProcessorInternalStatusCodes != null && !currentPaymentProcessorInternalStatusCodes.isEmpty()) {
							for (com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode : currentPaymentProcessorInternalStatusCodes) {
								if (!currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode()
									.getPaymentProcessorStatusCode().equals(resourceProcessorCode.getCode())
									&& !codeModified) {
									throw new CustomBadRequestException(
										"This Payment Processor is already related to another Internal Status Code.");
								}
								Long internalStatusCodeId = currentPaymentProcessorInternalStatusCode
									.getInternalStatusCode() != null ? currentPaymentProcessorInternalStatusCode
											.getInternalStatusCode().getInternalStatusCodeId() : null;
								if (internalStatusCodeId != null) {
									internalSet.add(internalStatusCodeId);
								}	
							}
						}
						paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorStatusCode.setPaymentProcessorStatusCode(resourceProcessorCode.getCode());
						paymentProcessorStatusCode.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());
						
						if (!internalSet.contains(internalStatusCode.getInternalStatusCodeId())) {
							newPaymentProcessorStatusCode.add(paymentProcessorStatusCode);
						}
					}
					if (paymentProcessorStatusCode.getPaymentProcessorStatusCodeId() != null) {
						// update payment processor status code
						paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.update(paymentProcessorStatusCode);
					} else {
						paymentProcessorStatusCode = paymentProcessorStatusCodeDAO.save(paymentProcessorStatusCode);
					}
					newMapOfPaymentProcessorStatusCodes.put(paymentProcessorStatusCode.getPaymentProcessorStatusCodeId(),paymentProcessorStatusCode);
				} else {
					if (resourceProcessorCode.getCode().isEmpty() && resourceProcessorCode.getDescription().isEmpty()) {
						LOGGER.info("InternalStatusCodeService :: updateInternalStatusCode() : Removing payment processor code");
					} else {
						throw new CustomBadRequestException(
								"Unable to save Payment Processor code with code or description empty.");

					}
				}
			}

			// Update information from current payment processor merchants
			Iterator<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> iter = internalStatusCode.getPaymentProcessorInternalStatusCodes().iterator();
			while (iter.hasNext()) {
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode element = iter.next();
				com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode ppmr = newMapOfPaymentProcessorStatusCodes.get(element.getPaymentProcessorStatusCodeId());
				if (ppmr == null) {
					paymentProcessorStatusCodeToDelete.add(element.getPaymentProcessorStatusCodeId());
					iter.remove();
				} else {
					element.setPaymentProcessorStatusCodeId(ppmr.getPaymentProcessorStatusCodeId());
				}
			}
			
			LOGGER.debug("InternalStatusCodeService :: updateInternalStatusCode() : newPaymentProcessorStatusCode size : "+newPaymentProcessorStatusCode.size());
			// Add the new payment processor Status codes
			for (com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode current : newPaymentProcessorStatusCode) {
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode();
				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(current.getPaymentProcessorStatusCodeId());
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCodeIdToModify);
				internalStatusCode.getPaymentProcessorInternalStatusCodes().add(paymentProcessorInternalStatusCode);
			}

		}
		
		return internalStatusCodeDAO.update(internalStatusCode);
	}

	public void deleteInternalStatusCode(Long internalStatusCodeId) {
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCodeToDelete = internalStatusCodeDAO.findOne(internalStatusCodeId);

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

	public com.mcmcg.ico.bluefin.model.InternalStatusCode getInternalStatusCode(Long internalStatusCodeId) {
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO.findOne(internalStatusCodeId);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getInternalStatusCode() : internalStatusCode : {} ",internalStatusCode);
		}
		if (internalStatusCode != null) {
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> list = paymentProcessorInternalStatusCodeDAO.findAllForInternalStatusCodeId(internalStatusCodeId);
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
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO
				.findByInternalStatusCodeAndTransactionTypeName(statusCode, "SALE");
		LOGGER.debug("nternalStatusCodeService :: getLetterFromStatusCodeForSaleTransactions() : internalStatusCode : "+internalStatusCode);
		if (internalStatusCode == null)
			return "";
		else
			return internalStatusCode.getInternalStatusCategoryAbbr();
	}
}
