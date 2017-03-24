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

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.InternalResponseCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.persistent.jpa.InternalResponseCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorResponseCodeRepository;
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
	private InternalResponseCodeRepository internalResponseCodeRepository;
	
	@Autowired
	private PaymentProcessorResponseCodeRepository paymentProcessorResponseCodeRepository;
	@Autowired
	private PaymentProcessorService paymentProcessorService;
	@Autowired
	private TransactionTypeService transactionTypeService;

	public List<InternalResponseCode> getInternalResponseCodesByTransactionType(String transactionType) {
		// Get transactionType if null thrown an exception
		transactionTypeService.getTransactionTypeByType(transactionType);
		return internalResponseCodeRepository.findByTransactionTypeNameOrderByInternalResponseCodeAsc(transactionType);
	}

	public InternalResponseCode createInternalResponseCodes(InternalCodeResource internalResponseCodeResource) {

		// Get transactionType if null thrown an exception
		TransactionType transactionType = transactionTypeService
				.getTransactionTypeByType(internalResponseCodeResource.getTransactionTypeName());

		InternalResponseCode internalResponseCode = internalResponseCodeRepository
				.findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
						transactionType.getTransactionType());

		if (internalResponseCode != null) {
			throw new CustomBadRequestException(
					"Internal response code already exists and is assigned to this transaction type.");
		}

		LOGGER.info("Creating new internal response code {}", internalResponseCodeResource.getCode());
		internalResponseCode = new InternalResponseCode();
		internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
		internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
		internalResponseCode.setTransactionTypeName(transactionType.getTransactionType());
		internalResponseCode
				.setPaymentProcessorInternalResponseCodes(new ArrayList<PaymentProcessorInternalResponseCode>());
		internalResponseCode = internalResponseCodeRepository.save(internalResponseCode);

		if (!(internalResponseCodeResource.getPaymentProcessorCodes() == null
				|| internalResponseCodeResource.getPaymentProcessorCodes().isEmpty())) {
			List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = new ArrayList<PaymentProcessorInternalResponseCode>();
			for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource
					.getPaymentProcessorCodes()) {

				PaymentProcessor paymentProcessor = null;
//				paymentProcessorService
//						.getPaymentProcessorById(resourceProcessorCode.getPaymentProcessorId());

				PaymentProcessorResponseCode paymentProcessorResponseCode = null;
				Boolean codeModified = false;
				if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
					paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
							.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
									resourceProcessorCode.getCode(), transactionType.getTransactionType(),
									paymentProcessor);
				} else {
					Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
					paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
							.findOne(paymentProcessorCodeId);
					if (paymentProcessorResponseCode == null) {
						throw new CustomNotFoundException(
								"Payment Processor Response Code does not exist: " + paymentProcessorCodeId);
					} else if (!resourceProcessorCode.getCode()
							.equals(paymentProcessorResponseCode.getPaymentProcessorResponseCode())) {
						codeModified = true;
						if (paymentProcessorResponseCodeRepository
								.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
										resourceProcessorCode.getCode(), transactionType.getTransactionType(),
										paymentProcessor) != null) {
							throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()
									+ " is already used by other Payment Processor Response Code.");
						}
					}
				}

				if (paymentProcessorResponseCode == null) {
					LOGGER.info("Creating new payment processor response code {}", resourceProcessorCode.getCode());
					paymentProcessorResponseCode = new PaymentProcessorResponseCode();
				} else {
					Collection<PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
							.getInternalResponseCode();
					for (PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
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
				paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionType());

				paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
						.save(paymentProcessorResponseCode);
				PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new PaymentProcessorInternalResponseCode();
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
				paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
				paymentProcessorInternalResponseCodes.add(paymentProcessorInternalResponseCode);

			}
			internalResponseCode.getPaymentProcessorInternalResponseCodes().clear();
			internalResponseCode.getPaymentProcessorInternalResponseCodes()
					.addAll(paymentProcessorInternalResponseCodes);

		}
		return internalResponseCodeRepository.save(internalResponseCode);

	}

	public InternalResponseCode updateInternalResponseCode(UpdateInternalCodeResource internalResponseCodeResource) {

		Long internalCodeId = internalResponseCodeResource.getInternalCodeId();
		InternalResponseCode internalResponseCode = internalResponseCodeRepository.findOne(internalCodeId);
		if (internalResponseCode == null) {
			throw new CustomNotFoundException("Internal Response Code does not exist: " + internalCodeId);
		}

		// Get transactionType if null thrown an exception
		TransactionType transactionType = transactionTypeService
				.getTransactionTypeByType(internalResponseCodeResource.getTransactionTypeName());

		LOGGER.info("Updating internal response code {}", internalCodeId);

		// Just in case of modify the code of the Internal Response Code, verify
		// if the code is already assigned
		if (!internalResponseCodeResource.getCode().equals(internalResponseCode.getInternalResponseCode())) {
			InternalResponseCode existingInternalResponseCode = internalResponseCodeRepository
					.findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
							transactionType.getTransactionType());
			if (existingInternalResponseCode != null) {
				throw new CustomBadRequestException(
						"Another Internal response code already exists and is assigned to this transaction type.");
			}
		}

		internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
		internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
		internalResponseCode.setTransactionTypeName(transactionType.getTransactionType());

		Set<Long> paymentProcessorResponseCodeToDelete = new HashSet<Long>();
		if (internalResponseCodeResource.getPaymentProcessorCodes() != null
				|| !internalResponseCodeResource.getPaymentProcessorCodes().isEmpty()) {
			// New payment processor response codes that need to be created or
			// updated
			List<PaymentProcessorResponseCode> newPaymentProcessorResponseCode = new ArrayList<PaymentProcessorResponseCode>();

			// New payment processor response codes that need to be created or
			// updated
			Map<Long, PaymentProcessorResponseCode> newMapOfPaymentProcessorResponseCodes = new HashMap<Long, PaymentProcessorResponseCode>();

			for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource
					.getPaymentProcessorCodes()) {

				PaymentProcessor paymentProcessor = null;
//						paymentProcessorService
//						.getPaymentProcessorById(resourceProcessorCode.getPaymentProcessorId());

				if (!resourceProcessorCode.getCode().isEmpty() && !resourceProcessorCode.getDescription().isEmpty()) {
					PaymentProcessorResponseCode paymentProcessorResponseCode;
					Boolean codeModified = false;
					if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
						paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
								.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
										resourceProcessorCode.getCode(), transactionType.getTransactionType(),
										paymentProcessor);
					} else {
						Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
						paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
								.findOne(paymentProcessorCodeId);
						if (paymentProcessorResponseCode == null) {
							throw new CustomNotFoundException(
									"Payment Processor Response Code does not exist: " + paymentProcessorCodeId);
						} else if (!resourceProcessorCode.getCode()
								.equals(paymentProcessorResponseCode.getPaymentProcessorResponseCode())) {
							codeModified = true;
							if (paymentProcessorResponseCodeRepository
									.findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
											resourceProcessorCode.getCode(), transactionType.getTransactionType(),
											paymentProcessor) != null) {
								throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()
										+ " is already used by other Payment Processor Response Code.");
							}
						}
					}

					Set<Long> internalSet = new HashSet<Long>();
					if (paymentProcessorResponseCode == null) {
						LOGGER.info("Creating new payment processor response code {}", resourceProcessorCode.getCode());
						paymentProcessorResponseCode = new PaymentProcessorResponseCode();

						paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorResponseCode.setPaymentProcessorResponseCode(resourceProcessorCode.getCode());
						paymentProcessorResponseCode
								.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionType());

						newPaymentProcessorResponseCode.add(paymentProcessorResponseCode);
						newMapOfPaymentProcessorResponseCodes.put(
								paymentProcessorResponseCode.getPaymentProcessorResponseCodeId(),
								paymentProcessorResponseCode);
					} else {
						Collection<PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
								.getInternalResponseCode();
						for (PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
							if (!currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode()
									.getPaymentProcessorResponseCode().equals(resourceProcessorCode.getCode())
									&& !codeModified) {
								throw new CustomBadRequestException(
										"This Payment Processor is already related to another Internal Response Code.");
							}
							InternalResponseCode current = currentPaymentProcessorInternalResponseCode
									.getInternalResponseCode();
							internalSet.add(current.getInternalResponseCodeId());
						}
						paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
						paymentProcessorResponseCode.setPaymentProcessorResponseCode(resourceProcessorCode.getCode());
						paymentProcessorResponseCode
								.setPaymentProcessorResponseCodeDescription(resourceProcessorCode.getDescription());
						paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionType());
						newMapOfPaymentProcessorResponseCodes.put(
								paymentProcessorResponseCode.getPaymentProcessorResponseCodeId(),
								paymentProcessorResponseCode);
						if (!internalSet.contains(internalResponseCode.getInternalResponseCodeId())) {
							newPaymentProcessorResponseCode.add(paymentProcessorResponseCode);
						}

					}
					paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
							.save(paymentProcessorResponseCode);
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

			// Add the new payment processor response codes
			for (PaymentProcessorResponseCode current : newPaymentProcessorResponseCode) {
				PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new PaymentProcessorInternalResponseCode();
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(current);

				internalResponseCode.addPaymentProcessorInternalResponseCode(paymentProcessorInternalResponseCode);
			}

		}
		InternalResponseCode result = internalResponseCodeRepository.save(internalResponseCode);

		if (paymentProcessorResponseCodeToDelete != null && !paymentProcessorResponseCodeToDelete.isEmpty()) {
			List<PaymentProcessorResponseCode> paymentProcessorResponseCodeEntitiesToDelete = paymentProcessorResponseCodeRepository
					.findAll(paymentProcessorResponseCodeToDelete);
			paymentProcessorResponseCodeRepository.delete(paymentProcessorResponseCodeEntitiesToDelete);
		}
		return result;
	}

	public void deleteInternalResponseCode(Long id) {
		InternalResponseCode internalResponseCodeToDelete = internalResponseCodeRepository.findOne(id);

		if (internalResponseCodeToDelete == null) {
			throw new CustomNotFoundException(
					String.format("Unable to find internal response code with id = [%s]", id));
		}
		List<PaymentProcessorResponseCode> paymentProcessorResponseCodeToDelete = new ArrayList<PaymentProcessorResponseCode>();
		for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : internalResponseCodeToDelete
				.getPaymentProcessorInternalResponseCodes()) {
			paymentProcessorResponseCodeToDelete
					.add(paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode());
		}
		internalResponseCodeToDelete.getPaymentProcessorInternalResponseCodes().clear();
		internalResponseCodeRepository.delete(internalResponseCodeToDelete);
		paymentProcessorResponseCodeRepository.delete(paymentProcessorResponseCodeToDelete);
	}
}
