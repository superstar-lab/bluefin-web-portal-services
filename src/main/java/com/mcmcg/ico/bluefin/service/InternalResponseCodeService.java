package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.InternalResponseCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.persistent.TransactionType;
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
        transactionTypeService.getTransactionTypeByName(transactionType);
        return internalResponseCodeRepository.findByTransactionTypeName(transactionType);
    }

    public InternalResponseCode createInternalResponseCodes(InternalCodeResource internalResponseCodeResource) {

        // Get transactionType if null thrown an exception
        TransactionType transactionType = transactionTypeService
                .getTransactionTypeByName(internalResponseCodeResource.getTransactionTypeName());

        InternalResponseCode internalResponseCode = internalResponseCodeRepository
                .findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(),
                        transactionType.getTransactionTypeName());

        if (internalResponseCode != null) {
            throw new CustomBadRequestException(
                    "Internal response code already exists and is assigned to this transaction type.");
        }

        LOGGER.info("Creating new internal response code {}", internalResponseCodeResource.getCode());
        internalResponseCode = new InternalResponseCode();
        internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
        internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
        internalResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());
        internalResponseCode
                .setPaymentProcessorInternalResponseCodes(new ArrayList<PaymentProcessorInternalResponseCode>());
        internalResponseCode = internalResponseCodeRepository.save(internalResponseCode);

        if (!(internalResponseCodeResource.getPaymentProcessorCodes() == null
                || internalResponseCodeResource.getPaymentProcessorCodes().isEmpty())) {
            List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = new ArrayList<PaymentProcessorInternalResponseCode>();
            for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource
                    .getPaymentProcessorCodes()) {

                PaymentProcessor paymentProcessor = paymentProcessorService
                        .getPaymentProcessorById(resourceProcessorCode.getPaymentProcessorId());

                PaymentProcessorResponseCode paymentProcessorResponseCode = null;
                if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
                    paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
                            .findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
                                    resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
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
                        if (paymentProcessorResponseCodeRepository
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
                    paymentProcessorResponseCode = new PaymentProcessorResponseCode();
                } else {
                    Collection<PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
                            .getInternalResponseCode();
                    for (PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
                        if (!currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode()
                                .equals(internalResponseCodeResource.getCode())) {
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
                .getTransactionTypeByName(internalResponseCodeResource.getTransactionTypeName());

        LOGGER.info("Updating internal response code {}", internalCodeId);

        // Just in case of modify the code of the Internal Response Code, verify
        // if the code is already assigned
        if (!internalResponseCodeResource.getCode().equals(internalResponseCode.getInternalResponseCode())) {
            InternalResponseCode existingInternalResponseCode = internalResponseCodeRepository
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
        internalResponseCode
                .setPaymentProcessorInternalResponseCodes(new ArrayList<PaymentProcessorInternalResponseCode>());
        internalResponseCode = internalResponseCodeRepository.save(internalResponseCode);

        if (internalResponseCodeResource.getPaymentProcessorCodes() != null
                || !internalResponseCodeResource.getPaymentProcessorCodes().isEmpty()) {
            List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = new ArrayList<PaymentProcessorInternalResponseCode>();
            for (PaymentProcessorCodeResource resourceProcessorCode : internalResponseCodeResource
                    .getPaymentProcessorCodes()) {

                PaymentProcessor paymentProcessor = paymentProcessorService
                        .getPaymentProcessorById(resourceProcessorCode.getPaymentProcessorId());

                PaymentProcessorResponseCode paymentProcessorResponseCode = null;
                if (resourceProcessorCode.getPaymentProcessorCodeId() == null) {
                    paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
                            .findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
                                    resourceProcessorCode.getCode(), transactionType.getTransactionTypeName(),
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
                        if (paymentProcessorResponseCodeRepository
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
                    paymentProcessorResponseCode = new PaymentProcessorResponseCode();
                } else {
                    Collection<PaymentProcessorInternalResponseCode> currentPaymentProcessorInternalResponseCodes = paymentProcessorResponseCode
                            .getInternalResponseCode();
                    for (PaymentProcessorInternalResponseCode currentPaymentProcessorInternalResponseCode : currentPaymentProcessorInternalResponseCodes) {
                        if (!currentPaymentProcessorInternalResponseCode.getPaymentProcessorResponseCode()
                                .equals(internalResponseCodeResource.getCode())) {
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

    public void deleteInternalResponseCode(Long id) {
        InternalResponseCode internalResponseCodeToDelete = internalResponseCodeRepository.findOne(id);

        if (internalResponseCodeToDelete == null) {
            throw new CustomNotFoundException(
                    String.format("Unable to find internal response code with id = [%s]", id));
        }
        internalResponseCodeToDelete.getPaymentProcessorInternalResponseCodes().clear();
        internalResponseCodeRepository.delete(internalResponseCodeToDelete);
    }
}
