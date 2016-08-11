package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;
import com.mcmcg.ico.bluefin.persistent.jpa.InternalStatusCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorInternalStatusCodeRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorStatusCodeRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;

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
    @Autowired
    private TransactionTypeService transactionTypeService;

    public List<InternalStatusCode> getInternalStatusCodes() {
        return internalStatusCodeRepository.findAll();
    }

    public InternalStatusCode upsertInternalStatusCodes(InternalCodeResource internalStatusCodeResource) {

        return null;
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
