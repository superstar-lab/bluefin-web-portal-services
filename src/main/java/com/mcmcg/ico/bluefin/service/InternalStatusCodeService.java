package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.persistent.jpa.InternalStatusCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorInternalStatusCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorStatusCodeRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorCodeResource;

@Service
@Transactional
public class InternalStatusCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalStatusCodeService.class);

    @Autowired
    private InternalStatusCodeRepository internalStatusCodeRepository;
    @Autowired
    private PaymentProcessorStatusCodeRepository paymentProcessorStatusCodeRepository;
    @Autowired
    private PaymentProcessorRepository paymentProcessorRepository;
    @Autowired
    private PaymentProcessorInternalStatusCodeRepository paymentProcessorInternalStatusCodeRepository;

    public List<InternalStatusCode> getInternalStatusCodes() {
        return internalStatusCodeRepository.findAll();
    }

    public Set<InternalStatusCode> getInternalStatusCodesByPaymentProcessorId(Long paymentProcessorId) {

        PaymentProcessor paymentProcessor = paymentProcessorRepository.findOne(paymentProcessorId);
        if (paymentProcessor == null) {
            throw new CustomBadRequestException("Invalid payment processor");
        }
        Set<InternalStatusCode> internalCodesResult = new HashSet<InternalStatusCode>();

        List<PaymentProcessorStatusCode> paymentProcessorStatusCodes = paymentProcessorStatusCodeRepository
                .findByPaymentProcessor(paymentProcessor);

        for (PaymentProcessorStatusCode paymentProcessorStatusCode : paymentProcessorStatusCodes) {
            Collection<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = paymentProcessorStatusCode
                    .getInternalStatusCode();
            for (PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode : paymentProcessorInternalStatusCodes) {
                internalCodesResult.add(paymentProcessorInternalStatusCode.getInternalStatusCode());
            }
        }

        return internalCodesResult;
    }

    public InternalStatusCode upsertInternalStatusCodes(InternalCodeResource internalStatusCodeResource) {

        PaymentProcessor paymentProcessor = paymentProcessorRepository
                .findOne(internalStatusCodeResource.getPaymentProcessorCode().getPaymentProcessorId());
        if (paymentProcessor == null) {
            throw new CustomBadRequestException("Invalid payment processor");
        }

        InternalStatusCode internalStatusCode = internalStatusCodeRepository
                .findByInternalStatusCode(internalStatusCodeResource.getCode());
        PaymentProcessorCodeResource paymentProcessorStatusCodeResource = internalStatusCodeResource
                .getPaymentProcessorCode();

        if (internalStatusCode == null) {
            LOGGER.info("Creating new internal status code {}", internalStatusCodeResource.getCode());
            internalStatusCode = new InternalStatusCode();
            internalStatusCode
                    .setPaymentProcessorInternalStatusCodes(new ArrayList<PaymentProcessorInternalStatusCode>());

        } else {
            LOGGER.info("Updating internal status code {}", internalStatusCodeResource.getCode());
            List<PaymentProcessorStatusCode> processorCodes = paymentProcessorStatusCodeRepository
                    .findByPaymentProcessor(paymentProcessor);
            for (PaymentProcessorStatusCode paymentProcessorStatusCode : processorCodes) {
                Collection<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = paymentProcessorStatusCode
                        .getInternalStatusCode();
                for (PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode : paymentProcessorInternalStatusCodes) {
                    if (paymentProcessorInternalStatusCode.getInternalStatusCode().getInternalStatusCode()
                            .equals(internalStatusCodeResource.getCode())
                            && !paymentProcessorInternalStatusCode.getPaymentProcessorStatusCode()
                                    .getPaymentProcessorStatusCode()
                                    .equals(internalStatusCodeResource.getPaymentProcessorCode().getCode())) {
                        throw new CustomBadRequestException(
                                "This Payment Processor is already related to another Internal Status Code.");
                    }
                }

            }

        }
        internalStatusCode.setInternalStatusCode(internalStatusCodeResource.getCode());
        internalStatusCode.setInternalStatusCodeDescription(internalStatusCodeResource.getDescription());

        if (paymentProcessorStatusCodeResource != null) {
            PaymentProcessorStatusCode paymentProcessorStatusCode = paymentProcessorStatusCodeRepository
                    .findByPaymentProcessorStatusCode(paymentProcessorStatusCodeResource.getCode());

            Boolean creatingPaymentProcessor = false;
            if (paymentProcessorStatusCode == null) {
                LOGGER.info("Creating new payment processor status code {}", internalStatusCodeResource.getCode());
                paymentProcessorStatusCode = new PaymentProcessorStatusCode();
                creatingPaymentProcessor = true;
            } else {
                LOGGER.info("Updating payment processor status code {}", internalStatusCodeResource.getCode());
            }

            paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
            paymentProcessorStatusCode.setPaymentProcessorStatusCode(paymentProcessorStatusCodeResource.getCode());
            paymentProcessorStatusCode
                    .setPaymentProcessorStatusDescription(paymentProcessorStatusCodeResource.getDescription());

            paymentProcessorStatusCode = paymentProcessorStatusCodeRepository.save(paymentProcessorStatusCode);
            internalStatusCode = internalStatusCodeRepository.save(internalStatusCode);

            if (creatingPaymentProcessor) {
                PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
                paymentProcessorInternalStatusCode.setInternalStatusCode(internalStatusCode);
                paymentProcessorInternalStatusCode.setPaymentProcessorStatusCode(paymentProcessorStatusCode);
                paymentProcessorInternalStatusCodeRepository.save(paymentProcessorInternalStatusCode);

                internalStatusCode.getPaymentProcessorInternalStatusCodes().add(paymentProcessorInternalStatusCode);
            }
        }
        return internalStatusCodeRepository.save(internalStatusCode);

    }

    public void deleteInternalStatusCode(Long id) {
        InternalStatusCode internalStatusCodeToDelete = internalStatusCodeRepository.findOne(id);

        if (internalStatusCodeToDelete == null) {
            throw new CustomNotFoundException(String.format("Unable to find internal status code with id = [%s]", id));
        }
        internalStatusCodeToDelete.getPaymentProcessorInternalStatusCodes().clear();
        internalStatusCodeRepository.delete(internalStatusCodeToDelete);
    }
}
