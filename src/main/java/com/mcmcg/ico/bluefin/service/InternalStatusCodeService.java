package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.persistent.TransactionType;
import com.mcmcg.ico.bluefin.persistent.jpa.InternalStatusCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorStatusCodeRepository;
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
    private InternalStatusCodeRepository internalStatusCodeRepository;
    @Autowired
    private PaymentProcessorStatusCodeRepository paymentProcessorStatusCodeRepository;
    @Autowired
    private PaymentProcessorService paymentProcessorService;
    @Autowired
    private TransactionTypeService transactionTypeService;

    public List<InternalStatusCode> getInternalStatusCodesByTransactionType(String transactionType) {
        // Get transactionType if null thrown an exception
        transactionTypeService.getTransactionTypeByName(transactionType);
        return internalStatusCodeRepository.findByTransactionTypeName(transactionType);
    }

    public InternalStatusCode createInternalStatusCodes(InternalCodeResource internalStatusCodeResource) {

        // Get transactionType if null thrown an exception
        TransactionType transactionType = transactionTypeService
                .getTransactionTypeByName(internalStatusCodeResource.getTransactionTypeName());

        InternalStatusCode internalStatusCode = internalStatusCodeRepository
                .findByInternalStatusCodeAndTransactionTypeName(internalStatusCodeResource.getCode(),
                        transactionType.getTransactionTypeName());

        if (internalStatusCode != null) {
            throw new CustomBadRequestException(
                    "Internal Status code already exists and is assigned to this transaction type.");
        }

        LOGGER.info("Creating new internal Status code {}", internalStatusCodeResource.getCode());
        internalStatusCode = new InternalStatusCode();
        internalStatusCode.setInternalStatusCode(internalStatusCodeResource.getCode());
        internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());
        internalStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());
        internalStatusCode
                .setPaymentProcessorInternalStatusCodes(new ArrayList<PaymentProcessorInternalStatusCode>());
        internalStatusCode = internalStatusCodeRepository.save(internalStatusCode);

        if (!(internalStatusCodeResource.getPaymentProcessorCodes() == null
                || internalStatusCodeResource.getPaymentProcessorCodes().isEmpty())) {
            List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = new ArrayList<PaymentProcessorInternalStatusCode>();
            for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource
                    .getPaymentProcessorCodes()) {

                PaymentProcessor paymentProcessor = paymentProcessorService
                        .getPaymentProcessorById(resourceProcessorCode.getPaymentProcessorId());

                PaymentProcessorStatusCode paymentProcessorStatusCode = null;
                Boolean codeModified = false;
                if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
                    paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                            .findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
                                    resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
                                    paymentProcessor);
                } else {
                    Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
                    paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                            .findOne(paymentProcessorCodeId);
                    if (paymentProcessorStatusCode == null) {
                        throw new CustomNotFoundException(
                                "Payment Processor Status Code does not exist: " + paymentProcessorCodeId);
                    } else if (!resourceProcessorCode.getCode()
                            .equals(paymentProcessorStatusCode.getPaymentProcessorStatusCode())) {
                        codeModified = true;
                        if (paymentProcessorStatusCodeRepository
                                .findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
                                        resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
                                        paymentProcessor) != null) {
                            throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()
                                    + " is already used by other Payment Processor Status Code.");
                        }
                    }
                }

                if (paymentProcessorStatusCode == null) {
                    LOGGER.info("Creating new payment processor Status code {}", resourceProcessorCode.getCode());
                    paymentProcessorStatusCode = new PaymentProcessorStatusCode();
                } else {
                    Collection<PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
                            .getInternalStatusCode();
                    for (PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode : currentPaymentProcessorInternalStatusCodes) {
                        if (!currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode()
                                .equals(internalStatusCodeResource.getCode()) && !codeModified) {
                            throw new CustomBadRequestException(
                                    "This Payment Processor is already related to another Internal Status Code.");
                        }
                    }
                }

                paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
                paymentProcessorStatusCode.setPaymentProcessorStatusCode(resourceProcessorCode.getCode());
                paymentProcessorStatusCode
                        .setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
                paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());

                paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                        .save(paymentProcessorStatusCode);
                PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
                paymentProcessorInternalStatusCode.setPaymentProcessorStatusCode(paymentProcessorStatusCode);
                paymentProcessorInternalStatusCode.setInternalStatusCode(internalStatusCode);
                paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);

            }
            internalStatusCode.getPaymentProcessorInternalStatusCodes().clear();
            internalStatusCode.getPaymentProcessorInternalStatusCodes()
                    .addAll(paymentProcessorInternalStatusCodes);

        }
        return internalStatusCodeRepository.save(internalStatusCode);

    }

    public InternalStatusCode updateInternalStatusCode(UpdateInternalCodeResource internalStatusCodeResource) {

        Long internalCodeId = internalStatusCodeResource.getInternalCodeId();
        InternalStatusCode internalStatusCode = internalStatusCodeRepository.findOne(internalCodeId);
        if (internalStatusCode == null) {
            throw new CustomNotFoundException("Internal Status Code does not exist: " + internalCodeId);
        }

        // Get transactionType if null thrown an exception
        TransactionType transactionType = transactionTypeService
                .getTransactionTypeByName(internalStatusCodeResource.getTransactionTypeName());

        LOGGER.info("Updating internal Status code {}", internalCodeId);

        // Just in case of modify the code of the Internal Status Code, verify
        // if the code is already assigned
        if (!internalStatusCodeResource.getCode().equals(internalStatusCode.getInternalStatusCode())) {
            InternalStatusCode existingInternalStatusCode = internalStatusCodeRepository
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

        if (internalStatusCodeResource.getPaymentProcessorCodes() != null
                || !internalStatusCodeResource.getPaymentProcessorCodes().isEmpty()) {
            // New payment processor Status codes that need to be created or
            // updated
            List<PaymentProcessorStatusCode> newPaymentProcessorStatusCode = new ArrayList<PaymentProcessorStatusCode>();

            // New payment processor Status codes that need to be created or
            // updated
            Map<Long, PaymentProcessorStatusCode> newMapOfPaymentProcessorStatusCodes = new HashMap<Long, PaymentProcessorStatusCode>();

            for (PaymentProcessorCodeResource resourceProcessorCode : internalStatusCodeResource
                    .getPaymentProcessorCodes()) {

                PaymentProcessor paymentProcessor = paymentProcessorService
                        .getPaymentProcessorById(resourceProcessorCode.getPaymentProcessorId());

                PaymentProcessorStatusCode paymentProcessorStatusCode;
                Boolean codeModified = false;
                if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
                    paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                            .findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
                                    resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
                                    paymentProcessor);
                } else {
                    Long paymentProcessorCodeId = resourceProcessorCode.getPaymentProcessorCodeId();
                    paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                            .findOne(paymentProcessorCodeId);
                    if (paymentProcessorStatusCode == null) {
                        throw new CustomNotFoundException(
                                "Payment Processor Status Code does not exist: " + paymentProcessorCodeId);
                    } else if (!resourceProcessorCode.getCode()
                            .equals(paymentProcessorStatusCode.getPaymentProcessorStatusCode())) {
                        codeModified = true;
                        if (paymentProcessorStatusCodeRepository
                                .findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
                                        resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
                                        paymentProcessor) != null) {
                            throw new CustomBadRequestException("The code " + resourceProcessorCode.getCode()
                                    + " is already used by other Payment Processor Status Code.");
                        }
                    }
                }

                if (paymentProcessorStatusCode == null) {
                    LOGGER.info("Creating new payment processor Status code {}", resourceProcessorCode.getCode());
                    paymentProcessorStatusCode = new PaymentProcessorStatusCode();

                } else {
                    Collection<PaymentProcessorInternalStatusCode> currentPaymentProcessorInternalStatusCodes = paymentProcessorStatusCode
                            .getInternalStatusCode();
                    for (PaymentProcessorInternalStatusCode currentPaymentProcessorInternalStatusCode : currentPaymentProcessorInternalStatusCodes) {
                        if (!currentPaymentProcessorInternalStatusCode.getPaymentProcessorStatusCode()
                                .getPaymentProcessorStatusCode().equals(resourceProcessorCode.getCode())
                                && !codeModified) {
                            throw new CustomBadRequestException(
                                    "This Payment Processor is already related to another Internal Status Code.");
                        }
                    }

                }

                paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
                paymentProcessorStatusCode.setPaymentProcessorStatusCode(resourceProcessorCode.getCode());
                paymentProcessorStatusCode
                        .setPaymentProcessorStatusCodeDescription(resourceProcessorCode.getDescription());
                paymentProcessorStatusCode.setTransactionTypeName(transactionType.getTransactionTypeName());

                newPaymentProcessorStatusCode.add(paymentProcessorStatusCode);
                newMapOfPaymentProcessorStatusCodes.put(
                        paymentProcessorStatusCode.getPaymentProcessorStatusCodeId(), paymentProcessorStatusCode);

                paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                        .save(paymentProcessorStatusCode);
            }

            // Update information from current payment processor merchants
            Iterator<PaymentProcessorInternalStatusCode> iter = internalStatusCode
                    .getPaymentProcessorInternalStatusCodes().iterator();
            while (iter.hasNext()) {
                PaymentProcessorInternalStatusCode element = iter.next();

                PaymentProcessorStatusCode ppmr = newMapOfPaymentProcessorStatusCodes
                        .get(element.getPaymentProcessorInternalStatusCodeId());
                if (ppmr == null) {
                    iter.remove();
                } else {
                    element.setPaymentProcessorStatusCode(ppmr);
                }
            }

            // Add the new payment processor Status codes
            for (PaymentProcessorStatusCode current : newPaymentProcessorStatusCode) {
                PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
                paymentProcessorInternalStatusCode.setPaymentProcessorStatusCode(current);

                internalStatusCode.addPaymentProcessorInternalStatusCode(paymentProcessorInternalStatusCode);
            }

        }
        return internalStatusCodeRepository.save(internalStatusCode);
    }

    public void deleteInternalStatusCode(Long id) {
        InternalStatusCode internalStatusCodeToDelete = internalStatusCodeRepository.findOne(id);

        if (internalStatusCodeToDelete == null) {
            throw new CustomNotFoundException(
                    String.format("Unable to find internal Status code with id = [%s]", id));
        }
        internalStatusCodeToDelete.getPaymentProcessorInternalStatusCodes().clear();
        internalStatusCodeRepository.delete(internalStatusCodeToDelete);
    }
}
