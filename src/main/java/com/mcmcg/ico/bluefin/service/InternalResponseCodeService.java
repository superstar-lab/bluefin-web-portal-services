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

	public List<com.mcmcg.ico.bluefin.model.InternalResponseCode> getInternalResponseCodesByTransactionType(String transactionType) {
		// Get transactionType if null thrown an exception
	
		//transactionTypeDAO.getTransactionTypeByType(transactionType);
		List<com.mcmcg.ico.bluefin.model.InternalResponseCode> internalResponseCodeList =internalResponseCodeDAO.findByTransactionTypeNameOrderByInternalResponseCodeAsc(transactionType);
		if(null != internalResponseCodeList && !internalResponseCodeList.isEmpty()){
			for (com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode : internalResponseCodeList) {
				internalResponseCode.setPaymentProcessorInternalResponseCodes(paymentProcessorInternalResponseCodeDAO.findPaymentProcessorInternalResponseCodeListByInternalResponseCodeId(internalResponseCode.getInternalResponseCodeId()));
				for (com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : internalResponseCode.getPaymentProcessorInternalResponseCodes()){
					Long paymentProcessorResponseCodeId = paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId();
					com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.findOne(paymentProcessorResponseCodeId);
					paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
					paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
				}
			}
		}
		return internalResponseCodeList;
	}

	public com.mcmcg.ico.bluefin.model.InternalResponseCode createInternalResponseCodes(InternalCodeResource internalResponseCodeResource,String userName) {

		// Get transactionType if null thrown an exception
		com.mcmcg.ico.bluefin.model.TransactionType transactionType = transactionTypeDAO
				.findByTransactionType(internalResponseCodeResource.getTransactionTypeName());

		com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode = internalResponseCodeDAO
				.findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
						transactionType.getTransactionTypeName());

		if (internalResponseCode != null) {
			throw new CustomBadRequestException(
					"Internal response code already exists and is assigned to this transaction type.");
		}

		LOGGER.info("Creating new internal response code {}", internalResponseCodeResource.getCode());
		internalResponseCode = new com.mcmcg.ico.bluefin.model.InternalResponseCode();
		internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
		internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
		internalResponseCode.setLastModifiedBy(userName);
		internalResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());
		internalResponseCode
				.setPaymentProcessorInternalResponseCodes(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>());

		if (!(internalResponseCodeResource.getPaymentProcessorCodes() == null
				|| internalResponseCodeResource.getPaymentProcessorCodes().isEmpty())) {
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>();
			for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource
					.getPaymentProcessorCodes()) {

				com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());

				com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode = null;
				Boolean codeModified = false;
				if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
					paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
							.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
									resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
									paymentProcessor);
				} else {
					Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
					paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
							.findOne(paymentProcessorCodeId);
					if (paymentProcessorResponseCode == null) {
						throw new CustomNotFoundException(
								"Payment Processor Response Code does not exist: " + paymentProcessorCodeId);
					} else if (!resourceProcessorCode.getCode()
							.equals(paymentProcessorResponseCode.getPaymentProcessorResponseCode())) {
						codeModified = true;
						if (paymentProcessorResponseCodeDAO
								.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
										resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
										paymentProcessor) != null) {
							throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()
									+ " is already used by other Payment Processor Response Code.");
						}
					}
				}

				if (paymentProcessorResponseCode == null) {
					LOGGER.info("Creating new payment processor response code {}", resourceProcessorCode.getCode());
					paymentProcessorResponseCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode();
				} else {
					Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
							.getInternalResponseCode();
					for (com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
						if (!currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode()
								.equals(internalResponseCodeResource.getCode()) && !codeModified) {
							throw new CustomBadRequestException(
									"This Payment Processor is already related to another Internal Response Code.");
						}
					}
				}

				paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
				paymentProcessorResponseCode.setPaymentProcessorResponseCode(resourceProcessorCode.getCode());
				paymentProcessorResponseCode
						.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
				paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

				// save or update payment processor status code..
				if (paymentProcessorResponseCode.getPaymentProcessorResponseCodeId() != null) {
					// update ..
					paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.update(paymentProcessorResponseCode);
				} else {
					paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
							.save(paymentProcessorResponseCode);
				}
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode();
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
				paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
				paymentProcessorInternalResponseCodes.add(paymentProcessorInternalResponseCode);

			}
			internalResponseCode.getPaymentProcessorInternalResponseCodes().clear();
			internalResponseCode.getPaymentProcessorInternalResponseCodes()
					.addAll(paymentProcessorInternalResponseCodes);

		}
		return internalResponseCodeDAO.save(internalResponseCode);

	}

	public com.mcmcg.ico.bluefin.model.InternalResponseCode updateInternalResponseCode(UpdateInternalCodeResource internalResponseCodeResource) {
		LOGGER.info("Updating InternalResponseCode Record, Requested Data="+(internalResponseCodeResource) + " , Child Items="+ ( internalResponseCodeResource.getPaymentProcessorCodes() != null ? internalResponseCodeResource.getPaymentProcessorCodes().size() : 0 ) );
		Long internalResponseCodeIdToModify = internalResponseCodeResource.getInternalCodeId();
		LOGGER.info("Internal Response CodeId to modify {}"+internalResponseCodeIdToModify);
		
		com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode = internalResponseCodeDAO.findOneWithChilds(internalResponseCodeIdToModify);
		if (internalResponseCode == null) {
			throw new CustomNotFoundException("Internal Response Code does not exist: " + internalResponseCodeIdToModify);
		}

		// Get transactionType if null thrown an exception
		com.mcmcg.ico.bluefin.model.TransactionType transactionType = transactionTypeDAO.findByTransactionType(internalResponseCodeResource.getTransactionTypeName());
		if (transactionType == null) {
			LOGGER.error("Transaction type {} not found",internalResponseCodeResource.getTransactionTypeName());
			throw new CustomBadRequestException("Transaction type "+internalResponseCodeResource.getTransactionTypeName()+" not exists.");
		}
		LOGGER.info("Updating internal response code {}", internalResponseCodeIdToModify);

		// Just in case of modify the code of the Internal Response Code, verify
		// if the code is already assigned
		if (!internalResponseCodeResource.getCode().equals(internalResponseCode.getInternalResponseCode())) {
			com.mcmcg.ico.bluefin.model.InternalResponseCode existingInternalResponseCode = internalResponseCodeDAO
					.findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
							transactionType.getTransactionTypeName());
			if (existingInternalResponseCode != null) {
				throw new CustomBadRequestException(
						"Another Internal response code already exists and is assigned to this transaction type.");
			}
		}

		internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
		internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
		internalResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

		Set<Long> paymentProcessorResponseCodeToDelete = new HashSet<Long>();
		if (internalResponseCodeResource.getPaymentProcessorCodes() != null	&& !internalResponseCodeResource.getPaymentProcessorCodes().isEmpty()) {
			LOGGER.debug("Number of payment processor codes to update="+internalResponseCodeResource.getPaymentProcessorCodes().size());
			// New payment processor response codes that need to be created or
			// updated
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> newPaymentProcessorResponseCode = new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>();

			// New payment processor response codes that need to be created or
			// updated
			Map<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> newMapOfPaymentProcessorResponseCodes = new HashMap<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>();

			for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource
					.getPaymentProcessorCodes()) {

				com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(resourceProcessorCode.getPaymentProcessorId());
				if (paymentProcessor == null) {
					LOGGER.error("Payment processor {} not found",resourceProcessorCode.getPaymentProcessorId());
					throw new CustomBadRequestException("Payment processor does not exists. Id="+resourceProcessorCode.getPaymentProcessorId());
				}
				if (!resourceProcessorCode.getCode().isEmpty() && !resourceProcessorCode.getDescription().isEmpty()) {
					com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode;
					Boolean codeModified = false;
					if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
						paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
								.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
										resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
										paymentProcessor);
					} else {
						Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
						paymentProcessorResponseCode = paymentProcessorResponseCodeDAO
								.findOne(paymentProcessorCodeId);
						
						if (paymentProcessorResponseCode == null) {
							throw new CustomNotFoundException(
									"Payment Processor Response Code does not exist: " + paymentProcessorCodeId);
						} else if (!resourceProcessorCode.getCode()
								.equals(paymentProcessorResponseCode.getPaymentProcessorResponseCode())) {
							codeModified = true;
							if (paymentProcessorResponseCodeDAO
									.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
											resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
											paymentProcessor) != null) {
								throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()
										+ " is already used by other Payment Processor Response Code.");
							}
						}
					}

					Set<Long> internalSet = new HashSet<Long>();
					if (paymentProcessorResponseCode == null) {
						LOGGER.info("Creating new payment processor response code {}", resourceProcessorCode.getCode());
						paymentProcessorResponseCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode();

						paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorResponseCode.setPaymentProcessorResponseCode(resourceProcessorCode.getCode());
						paymentProcessorResponseCode
								.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

						newPaymentProcessorResponseCode.add(paymentProcessorResponseCode);
					} else {
						Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
								.getInternalResponseCode();
						if (currentPaymentProcessorInternalResponseCodes != null && !currentPaymentProcessorInternalResponseCodes.isEmpty()) {
							for (com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
								if (!currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode()
										.getPaymentProcessorResponseCode().equals(resourceProcessorCode.getCode())
										&& !codeModified) {
									throw new CustomBadRequestException(
											"This Payment Processor is already related to another Internal Response Code.");
								}
								Long internalResponseCodeId = currentPaymentProcessorInternalResponseCode
										.getInternalResponseCode() != null ? currentPaymentProcessorInternalResponseCode
												.getInternalResponseCode().getInternalResponseCodeId() : null;
								if (internalResponseCodeId != null) {
										internalSet.add(internalResponseCodeId);
								}
							}
						}
						paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorResponseCode.setPaymentProcessorResponseCode(resourceProcessorCode.getCode());
						paymentProcessorResponseCode
								.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());
						
						if (!internalSet.contains(internalResponseCode.getInternalResponseCodeId())) {
							newPaymentProcessorResponseCode.add(paymentProcessorResponseCode);
						}

					}
					// update payment processor status code
					if (paymentProcessorResponseCode.getPaymentProcessorResponseCodeId() != null) {
						// update payment processor status code
						paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.update(paymentProcessorResponseCode);
					} else {
						paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.save(paymentProcessorResponseCode);
					}
					newMapOfPaymentProcessorResponseCodes.put(paymentProcessorResponseCode.getPaymentProcessorResponseCodeId(),paymentProcessorResponseCode);
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
			Iterator<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> iter = internalResponseCode
					.getPaymentProcessorInternalResponseCodes().iterator();
			while (iter.hasNext()) {
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode element = iter.next();

				com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode ppmr = newMapOfPaymentProcessorResponseCodes
						.get(element.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
				if (ppmr == null) {
					paymentProcessorResponseCodeToDelete
							.add(element.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
					iter.remove();
				} else {
					element.setPaymentProcessorResponseCode(ppmr);
				}
			}

			// Add the new payment processor response codes
			for (com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode current : newPaymentProcessorResponseCode) {
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode();
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCodeId(current.getPaymentProcessorResponseCodeId().longValue());
				paymentProcessorInternalResponseCode.setInternalResponseCodeId(internalResponseCodeIdToModify);
				internalResponseCode.getPaymentProcessorInternalResponseCodes().add(paymentProcessorInternalResponseCode);
			}

		}
		com.mcmcg.ico.bluefin.model.InternalResponseCode result = internalResponseCodeDAO.update(internalResponseCode);
		if (paymentProcessorResponseCodeToDelete != null && !paymentProcessorResponseCodeToDelete.isEmpty()) {
			//List<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> paymentProcessorResponseCodeEntitiesToDelete = paymentProcessorResponseCodeDAO
				//	.findAll(paymentProcessorResponseCodeToDelete);
	//		paymentProcessorResponseCodeDAO.delete(paymentProcessorResponseCodeEntitiesToDelete);
		}
		return result;
	}

	public void deleteInternalResponseCode(Long id) {
		com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCodeToDelete = internalResponseCodeDAO.findOne(id);

		if (internalResponseCodeToDelete == null) {
			throw new CustomNotFoundException(
					String.format("Unable to find internal response code with id = [%s]", id));
		}
		//List<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> paymentProcessorResponseCodeToDelete = new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>();
		List<Long > paymentProcessorResponseCodeIds = paymentProcessorInternalResponseCodeDAO.findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode(id);
		
		internalResponseCodeDAO.delete(internalResponseCodeToDelete);
		if(paymentProcessorResponseCodeIds != null && !paymentProcessorResponseCodeIds.isEmpty()){
			paymentProcessorInternalResponseCodeDAO.deletePaymentProcessorResponseCodeIds(paymentProcessorResponseCodeIds);
		}
		
	}
}
