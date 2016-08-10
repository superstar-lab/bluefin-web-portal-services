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
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorInternalResponseCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorResponseCodeRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorCodeResource;

@Service
@Transactional
public class InternalResponseCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalResponseCodeService.class);

    @Autowired
    private InternalResponseCodeRepository internalResponseCodeRepository;
    @Autowired
    private PaymentProcessorResponseCodeRepository paymentProcessorResponseCodeRepository;
    @Autowired
    private PaymentProcessorRepository paymentProcessorRepository;
    @Autowired
    private PaymentProcessorInternalResponseCodeRepository paymentProcessorInternalResponseCodeRepository;
    @Autowired
    private TransactionTypeService transactionTypeService;

    public List<InternalResponseCode> getInternalResponseCodes() {
        return internalResponseCodeRepository.findAll();
    }

    public InternalResponseCode upsertInternalResponseCodes(InternalCodeResource internalResponseCodeResource) {

        PaymentProcessor paymentProcessor = paymentProcessorRepository
                .findOne(internalResponseCodeResource.getPaymentProcessorCode().getPaymentProcessorId());
        if (paymentProcessor == null) {
            throw new CustomBadRequestException("Invalid payment processor");
        }

        TransactionType transactionType = transactionTypeService
                .getTransactionTypeById(internalResponseCodeResource.getTransactionTypeId());
        if (transactionType == null) {
            throw new CustomBadRequestException("Invalid transaction type");
        }

        InternalResponseCode internalResponseCode = internalResponseCodeRepository
                .findByInternalResponseCodeAndTransactionTypeName(internalResponseCodeResource.getCode(), transactionType.getTransactionTypeName());
        PaymentProcessorCodeResource paymentProcessorResponseCodeResource = internalResponseCodeResource
                .getPaymentProcessorCode();

        if (internalResponseCode == null) {
            LOGGER.info("Creating new internal response code {}", internalResponseCodeResource.getCode());
            internalResponseCode = new InternalResponseCode();
            internalResponseCode
                    .setPaymentProcessorInternalResponseCodes(new ArrayList<PaymentProcessorInternalResponseCode>());
        } else {
            LOGGER.info("Updating internal response code {}", internalResponseCodeResource.getCode());
            List<PaymentProcessorResponseCode> processorCodes = paymentProcessorResponseCodeRepository
                    .findByPaymentProcessorAndTransactionTypeName(paymentProcessor, transactionType.getTransactionTypeName());
            for (PaymentProcessorResponseCode paymentProcessorResponseCode : processorCodes) {
                Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = paymentProcessorResponseCode
                        .getInternalResponseCode();
                for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : paymentProcessorInternalResponseCodes) {
                    if (paymentProcessorInternalResponseCode.getInternalResponseCode().getInternalResponseCode()
                            .equals(internalResponseCodeResource.getCode())
                            && !paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode()
                                    .getPaymentProcessorResponseCode()
                                    .equals(internalResponseCodeResource.getPaymentProcessorCode().getCode())) {
                        throw new CustomBadRequestException(
                                "This Payment Processor is already related to another Internal Response Code.");
                    }
                }

            }

        }
        internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
        internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());
        internalResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

        if (paymentProcessorResponseCodeResource != null) {
            PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
                    .findByPaymentProcessorResponseCodeAndTransactionTypeName(
                            paymentProcessorResponseCodeResource.getCode(), transactionType.getTransactionTypeName());

            Boolean creatingPaymentProcessor = false;
            if (paymentProcessorResponseCode == null) {
                LOGGER.info("Creating new payment processor response code {}", internalResponseCodeResource.getCode());
                paymentProcessorResponseCode = new PaymentProcessorResponseCode();
                creatingPaymentProcessor = true;
            } else {
                LOGGER.info("Updating payment processor response code {}", internalResponseCodeResource.getCode());
            }

            paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
            paymentProcessorResponseCode
                    .setPaymentProcessorResponseCode(paymentProcessorResponseCodeResource.getCode());
            paymentProcessorResponseCode
                    .setPaymentProcessorResponseCodeDescription(paymentProcessorResponseCodeResource.getDescription());
            paymentProcessorResponseCode.setTransactionTypeName(transactionType.getTransactionTypeName());

            paymentProcessorResponseCode = paymentProcessorResponseCodeRepository.save(paymentProcessorResponseCode);
            internalResponseCode = internalResponseCodeRepository.save(internalResponseCode);

            if (creatingPaymentProcessor) {
                PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new PaymentProcessorInternalResponseCode();
                paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
                paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
                paymentProcessorInternalResponseCodeRepository.save(paymentProcessorInternalResponseCode);

                internalResponseCode.getPaymentProcessorInternalResponseCodes()
                        .add(paymentProcessorInternalResponseCode);
            }
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
