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
		//TODO - Dheeraj will verify.
		/*if ("ALL".equals(transactionType)) {
			
		}
		TransactionType transactionTypeObj = transactionTypeDAO.findByTransactionType(transactionType);
		if (transactionTypeObj == null) {
			throw new CustomBadRequestException("Transaction type not exists.");
		}*/
		List<com.mcmcg.ico.bluefin.model.InternalStatusCode> internalStatusCodeList = internalStatusCodeDAO.findByTransactionTypeNameOrderByInternalStatusCodeAsc(transactionType);
		if (internalStatusCodeList != null && !internalStatusCodeList.isEmpty()) {
			for(com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode : internalStatusCodeList){
				Long internalStatusCodeId = internalStatusCode.getInternalStatusCodeId();
				List<PaymentProcessorInternalStatusCode> list = paymentProcessorInternalStatusCodeDAO.findAllForInternalStatusCodeId(internalStatusCodeId);
				
				
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
			LOGGER.error("Transaction type {} not found",internalStatusCodeResource.getTransactionTypeName());
			throw new CustomBadRequestException("Transaction type not exists.");
		}
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO
				.findByInternalStatusCodeAndTransactionTypeName(internalStatusCodeResource.getCode(),
						transactionType.getTransactionTypeName());

		if (internalStatusCode != null) {
			throw new CustomBadRequestException(
					"Internal Status code already exists and is assigned to this transaction type.");
		}

		LOGGER.info("Creating new internal Status code {}", internalStatusCodeResource.getCode());
		internalStatusCode = new com.mcmcg.ico.bluefin.model.InternalStatusCode();
		internalStatusCode.setInternalStatusCode(internalStatusCodeResource.getCode());
		internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());
		internalStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());
		internalStatusCode.setPaymentProcessorInternalStatusCodes(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode>());
		internalStatusCode.setInternalStatusCategory(internalStatusCodeResource.getInternalStatusCategory());
		internalStatusCode.setInternalStatusCategoryAbbr(internalStatusCodeResource.getInternalStatusCategoryAbbr());
		internalStatusCode.setLastModifiedBy(currentLoginUserName);
		
		if ( internalStatusCodeResource.getPaymentProcessorCodes() != null && !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("Number of payment processor internal status codes="+internalStatusCodeResource.getPaymentProcessorCodes().size());
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode>();
			for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource.getPaymentProcessorCodes()) {
				LOGGER.debug("Payment processor internal status code="+resourceProcessorCode);
				// validate if payment processor is exists or not
				com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());
				if (paymentProcessor == null) {
					LOGGER.error("Payment processor {} not found",resourceProcessorCode.getPaymentProcessorId());
					throw new CustomBadRequestException("Payment processor does not exists. Id="+resourceProcessorCode.getPaymentProcessorId());
				}
				com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCode = null;
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
				
				if (paymentProcessorStatusCode == null) {
					LOGGER.info("Creating new payment processor Status code {}", resourceProcessorCode.getCode());
					paymentProcessorStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode();
				} else {
					Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
							.getInternalStatusCode();
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
//				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCode(paymentProcessorStatusCode.convertPersistentObjectToModelObject());
//				paymentProcessorInternalStatusCode.setInternalStatusCode(internalStatusCode);
				
				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(paymentProcessorStatusCode.getPaymentProcessorStatusCodeId());
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCode.getInternalStatusCodeId());
				paymentProcessorInternalStatusCode.setLastModifiedBy(currentLoginUserName);
				paymentProcessorInternalStatusCode.setCreatedDate(internalStatusCode.getCreatedDate());
				paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);

			}
			internalStatusCode.getPaymentProcessorInternalStatusCodes().addAll(paymentProcessorInternalStatusCodes);
			LOGGER.debug("No of childs added "+internalStatusCode.getPaymentProcessorInternalStatusCodes().size());
		} else {
			LOGGER.debug("No payment processor internal status codes found as child items");
		}
		// Finally creating internal status code with parent and child in single transaction
		internalStatusCode = internalStatusCodeDAO.save(internalStatusCode);
		return internalStatusCode;
	}

	public com.mcmcg.ico.bluefin.model.InternalStatusCode updateInternalStatusCode(UpdateInternalCodeResource internalStatusCodeResource,String currentLoginUserName) {
		LOGGER.info("Updating InternalStatusCode Record");
		LOGGER.debug("Requested Data="+(internalStatusCodeResource) + " , Child Items="+ ( internalStatusCodeResource.getPaymentProcessorCodes() != null ? internalStatusCodeResource.getPaymentProcessorCodes().size() : 0 ) );
		Long internalStatusCodeIdToModify = internalStatusCodeResource.getInternalCodeId();
		LOGGER.debug("Internal Status CodeId to modify {}"+internalStatusCodeIdToModify);
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO.findOneWithChilds(internalStatusCodeIdToModify);
		if (internalStatusCode == null) {
			throw new CustomNotFoundException("Internal Status Code does not exist: " + internalStatusCodeIdToModify);
		}
		// Get transactionType if null thrown an exception
		TransactionType transactionType = transactionTypeDAO.findByTransactionType(internalStatusCodeResource.getTransactionTypeName());
		if (transactionType == null) {
			LOGGER.error("Transaction type {} not found",internalStatusCodeResource.getTransactionTypeName());
			throw new CustomBadRequestException("Transaction type "+internalStatusCodeResource.getTransactionTypeName()+" not exists.");
		}
		LOGGER.info("Updating internal Status code");

		// Just in case of modify the code of the Internal Status Code, verify
		// if the code is already assigned
		if (!internalStatusCodeResource.getCode().equals(internalStatusCode.getInternalStatusCode())) {
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
		
		Set<Long> paymentProcessorStatusCodeToDelete = new HashSet<Long>();
		if (internalStatusCodeResource.getPaymentProcessorCodes() != null && !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("Number of payment processor codes to update="+internalStatusCodeResource.getPaymentProcessorCodes().size());
			// New payment processor Status codes that need to be created or
			// updated
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> newPaymentProcessorStatusCode = new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>();

			// New payment processor Status codes that need to be created or
			// updated
			Map<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> newMapOfPaymentProcessorStatusCodes = new HashMap<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>();

			for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource.getPaymentProcessorCodes()) {

				com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());
				if (paymentProcessor == null) {
					LOGGER.error("Payment processor {} not found",resourceProcessorCode.getPaymentProcessorId());
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

					Set<Long> internalSet = new HashSet<Long>();
					if (paymentProcessorStatusCode == null) {
						LOGGER.info("Creating new payment processor Status code {}", resourceProcessorCode.getCode());
						paymentProcessorStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode();
						paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorStatusCode.setPaymentProcessorStatusCode(resourceProcessorCode.getCode());
						paymentProcessorStatusCode.setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());

						newPaymentProcessorStatusCode.add(paymentProcessorStatusCode);
						// Dheeraj : as per my analysis if code enter in this block and then paymentProcessorStatusCode.getPaymentProcessorStatusCodeId() == null , so need to put entry after saving paymentProcessorStatusCode
//						newMapOfPaymentProcessorStatusCodes.put(paymentProcessorStatusCode.getPaymentProcessorStatusCodeId(),paymentProcessorStatusCode);

					} else {
						Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
								.getInternalStatusCode();
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
						LOGGER.info("Removing payment processor code");
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
			
			// Add the new payment processor Status codes
			for (com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode current : newPaymentProcessorStatusCode) {
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode();
//				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCode(current);
				paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(current.getPaymentProcessorStatusCodeId());
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCodeIdToModify);
				internalStatusCode.getPaymentProcessorInternalStatusCodes().add(paymentProcessorInternalStatusCode);
			}

		}
		
		com.mcmcg.ico.bluefin.model.InternalStatusCode result = internalStatusCodeDAO.update(internalStatusCode);

		if (paymentProcessorStatusCodeToDelete != null && !paymentProcessorStatusCodeToDelete.isEmpty()) {
			// finally deletes payment processor status codes
//			List<PaymentProcessorStatusCode> paymentProcessorStatusCodeEntitiesToDelete = paymentProcessorStatusCodeDAO.findAll(paymentProcessorStatusCodeToDelete);
//			paymentProcessorStatusCodeDAO.delete(paymentProcessorStatusCodeEntitiesToDelete);
		}

		return result;
	}

	public void deleteInternalStatusCode(Long internalStatusCodeId) {
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCodeToDelete = internalStatusCodeDAO.findOne(internalStatusCodeId);

		if (internalStatusCodeToDelete == null) {
			throw new CustomNotFoundException(String.format("Unable to find internal Status code with id = [%s]", internalStatusCodeId));
		}
		// need to find all payment processor status code ids which used by payment processor internal status code of requested internalStatusCodeId to delete
		List<Long> paymentProcessorStatusCodeIds = paymentProcessorInternalStatusCodeDAO.findPaymentProcessorStatusCodeIdsForInternalStatusCodeId(internalStatusCodeId);
		// First delete internal status code and payment processor internal status code
		internalStatusCodeDAO.delete(internalStatusCodeId);
		// Second delete all payment processor status code which were in used by deleted internal status code
		paymentProcessorInternalStatusCodeDAO.deletePaymentProcessorStatusCodeIds(paymentProcessorStatusCodeIds);
	}

	public com.mcmcg.ico.bluefin.model.InternalStatusCode getInternalStatusCode(Long internalStatusCodeId) {
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO.findOne(internalStatusCodeId);
		if (internalStatusCode != null) {
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode> list = paymentProcessorInternalStatusCodeDAO.findAllForInternalStatusCodeId(internalStatusCodeId);
			if (list != null && !list.isEmpty()) {
				internalStatusCode.setPaymentProcessorInternalStatusCodes(list);
			}
		}
		return internalStatusCode;
	}
	
	public String getLetterFromStatusCodeForSaleTransactions(String statusCode) {
		com.mcmcg.ico.bluefin.model.InternalStatusCode internalStatusCode = internalStatusCodeDAO
				.findByInternalStatusCodeAndTransactionTypeName(statusCode, "SALE");
		if (internalStatusCode == null)
			return "";
		else
			return internalStatusCode.getInternalStatusCategoryAbbr();
	}
}
