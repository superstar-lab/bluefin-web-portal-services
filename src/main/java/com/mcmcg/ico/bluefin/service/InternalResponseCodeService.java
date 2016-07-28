package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.InternalResponseCode;
import com.mcmcg.ico.bluefin.persistent.InternalResponseCodeCategory;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.persistent.jpa.InternalResponseCodeCategoryRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.InternalResponseCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorResponseCodeRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.InternalResponseCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorResponseCodeResource;

@Service
@Transactional
public class InternalResponseCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalResponseCodeService.class);

    @Autowired
    private InternalResponseCodeRepository internalResponseCodeRepository;
    @Autowired
    private PaymentProcessorResponseCodeRepository paymentProcessorResponseCodeRepository;
    @Autowired
    private InternalResponseCodeCategoryRepository internalResponseCodeCategoryRepository;
    @Autowired
    private PaymentProcessorRepository paymentProcessorRepository;

    public List<InternalResponseCode> getInternalResponseCodes() {
        return internalResponseCodeRepository.findAll();
    }

    public InternalResponseCode upsertInternalResponseCodes(InternalResponseCodeResource internalResponseCodeResource) {
        InternalResponseCodeCategory category = internalResponseCodeCategoryRepository
                .findOne(internalResponseCodeResource.getCategoryId());
        if (category == null) {
            throw new CustomBadRequestException("Invalid category");
        }

        InternalResponseCode internalResponseCode = internalResponseCodeRepository
                .findByInternalResponseCode(internalResponseCodeResource.getCode());
        PaymentProcessorResponseCodeResource paymentProcessorResponseCodeResource = internalResponseCodeResource
                .getPaymentProcessorResponseCode();

        if (internalResponseCode == null) {
            LOGGER.info("Creating new internal response code {}", internalResponseCodeResource.getCode());
            internalResponseCode = new InternalResponseCode();
        } else {
            LOGGER.info("Updating internal response code {}", internalResponseCodeResource.getCode());
            if (paymentProcessorResponseCodeRepository
                    .findByPaymentProcessorResponseCode(paymentProcessorResponseCodeResource.getCode()) != null) {
                throw new CustomBadRequestException(
                        "This Payment Processor Response Code is already related to another Internal Response Code.");
            }
            internalResponseCode.getPaymentProcessorInternalResponseCodes().clear();
        }
        internalResponseCode.setInternalResponseCodeCategory(category);
        internalResponseCode.setInternalResponseCode(internalResponseCodeResource.getCode());
        internalResponseCode.setInternalResponseCodeDescription(internalResponseCodeResource.getDescription());

        if (paymentProcessorResponseCodeResource != null) {
            PaymentProcessor paymentProcessor = paymentProcessorRepository
                    .findOne(paymentProcessorResponseCodeResource.getPaymentProcessorId());
            if (paymentProcessor == null) {
                throw new CustomBadRequestException("Invalid payment processor");
            }

            PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeRepository
                    .findByPaymentProcessorResponseCode(paymentProcessorResponseCodeResource.getCode());

            if (paymentProcessorResponseCode == null) {
                LOGGER.info("Creating new payment processor response code {}", internalResponseCodeResource.getCode());
                paymentProcessorResponseCode = new PaymentProcessorResponseCode();
            } else {
                LOGGER.info("Updating payment processor response code {}", internalResponseCodeResource.getCode());
            }
            internalResponseCode
                    .setPaymentProcessorInternalResponseCodes(new ArrayList<PaymentProcessorInternalResponseCode>());

            paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
            paymentProcessorResponseCode
                    .setPaymentProcessorResponseCode(paymentProcessorResponseCodeResource.getCode());
            paymentProcessorResponseCode
                    .setPaymentProcessorResponseCodeDescription(paymentProcessorResponseCodeResource.getDescription());

            paymentProcessorResponseCode = paymentProcessorResponseCodeRepository.save(paymentProcessorResponseCode);

            PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = new PaymentProcessorInternalResponseCode();
            paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
            paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);

            internalResponseCode.getPaymentProcessorInternalResponseCodes().add(paymentProcessorInternalResponseCode);
        }

        return internalResponseCodeRepository.save(internalResponseCode);
    }
}
