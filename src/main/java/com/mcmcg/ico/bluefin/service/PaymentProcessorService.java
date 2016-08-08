package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorMerchant;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.BasicPaymentProcessorResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorMerchantResource;

@Service
@Transactional
public class PaymentProcessorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorService.class);

    @Autowired
    private PaymentProcessorRepository paymentProcessorRepository;

    /**
     * This method will find a payment processor by its id, not found exception
     * if it does not exist
     * 
     * @param id
     * @return
     */
    public PaymentProcessor getPaymentProcessorById(final long id) {
        PaymentProcessor paymentProcessor = paymentProcessorRepository.findOne(id);

        if (paymentProcessor == null) {
            throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
        }
        return paymentProcessor;
    }

    /**
     * This method will return a list of all the payment processors
     * 
     * @return List with payment processors that match the criteria given, not
     *         found exception if not match found
     */
    public List<PaymentProcessor> getPaymentProcessors() {
        List<PaymentProcessor> result = paymentProcessorRepository.findAll();

        return result == null ? new ArrayList<PaymentProcessor>() : result;
    }

    /**
     * This method will create a new Payment Processor that does not exist
     * already, if name exists will throw a bad request exception
     * 
     * @param paymentProcessorResource
     * @return Payment processor created
     */
    public PaymentProcessor createPaymentProcessor(BasicPaymentProcessorResource paymentProcessorResource) {
        PaymentProcessor paymentProcessor = paymentProcessorResource.toPaymentProcessor();
        final String processorName = paymentProcessor.getProcessorName();

        if (existPaymentProcessorName(processorName)) {
            LOGGER.error("Unable to create Payment Processor, this processor already exists: [{}]", processorName);
            throw new CustomBadRequestException(String
                    .format("Unable to create Payment Processor, this processor already exists: %s", processorName));
        }
        return paymentProcessorRepository.save(paymentProcessor);
    }

    /**
     * Updates the payment processor if it exists by id, if not, an not found
     * exception will be thrown
     * 
     * @param paymentProcessorResource
     * @return updated PaymentProcessor
     */
    public PaymentProcessor updatePaymentProcessor(final long id,
            BasicPaymentProcessorResource paymentProcessorResource) {
        PaymentProcessor paymentProcessorToUpdate = getPaymentProcessorById(id);

        // Update fields for existing Payment Processor
        paymentProcessorToUpdate.setProcessorName(paymentProcessorResource.getProcessorName());
        paymentProcessorToUpdate.setIsActive(paymentProcessorResource.getIsActive());

        return paymentProcessorToUpdate;
    }

    /**
     * Add/remove payment processor merchants from a payment processor
     * 
     * @param id
     *            identifier of the payment processor
     * @param paymentProcessorMerchants
     *            list of payment processor merchants
     * @return updated payment processor
     * @throws CustomNotFoundException
     *             when payment processor not found
     */
    public PaymentProcessor updatePaymentProcessorMerchants(final long id,
            Set<PaymentProcessorMerchantResource> paymentProcessorMerchants) {
        // Verify if payment processor exists
        PaymentProcessor paymentProcessorToUpdate = getPaymentProcessorById(id);

        // Payment processor must be active
        if (!paymentProcessorToUpdate.isActive()) {
            LOGGER.error(
                    "Unable to map payment processor merchants because payment processor is NOT active.  Payment processor id = [{}]",
                    paymentProcessorToUpdate.getPaymentProcessorId());
            throw new CustomNotFoundException(String.format(
                    "Unable to map payment processor merchants because payment processor [%s] is NOT active.",
                    paymentProcessorToUpdate.getPaymentProcessorId()));
        }

        // User wants to clear payment processor merchants from payment
        // processor
        if (paymentProcessorMerchants.isEmpty()) {
            paymentProcessorToUpdate.getPaymentProcessorMerchants().clear();
            return paymentProcessorRepository.save(paymentProcessorToUpdate);
        }

        // New payment processor merchants that need to be created or updated
        Map<Long, PaymentProcessorMerchantResource> newMapOfPaymentProcessorMerchants = paymentProcessorMerchants
                .stream().collect(Collectors.toMap(PaymentProcessorMerchantResource::getLegalEntityAppId, p -> p));

        // Temporal list of legal entity app ids already updated
        Set<Long> PaymentProcessorMerchantsToKeep = new HashSet<Long>();

        // Update information from current payment processor merchants
        Iterator<PaymentProcessorMerchant> iter = paymentProcessorToUpdate.getPaymentProcessorMerchants().iterator();
        while (iter.hasNext()) {
            PaymentProcessorMerchant element = iter.next();

            PaymentProcessorMerchantResource ppmr = newMapOfPaymentProcessorMerchants
                    .get(element.getLegalEntityApp().getLegalEntityAppId());
            if (ppmr == null) {
                iter.remove();
            } else {
                element.setMerchantId(ppmr.getMerchantId());
                element.setTestOrProd(ppmr.getTestOrProd());
                PaymentProcessorMerchantsToKeep.add(ppmr.getLegalEntityAppId());
            }
        }

        // Add the new payment processor merchants
        for (Long legalEntityId : newMapOfPaymentProcessorMerchants.keySet()) {
            if (!PaymentProcessorMerchantsToKeep.contains(legalEntityId)) {
                paymentProcessorToUpdate.addPaymentProcessorMerchant(
                        newMapOfPaymentProcessorMerchants.get(legalEntityId).toPaymentProcessorMerchant());
            }
        }

        return paymentProcessorRepository.save(paymentProcessorToUpdate);
    }

    /**
     * Deletes a payment processor by id, not found exception will be thrown if
     * payment processor does not exists
     * 
     * @param id
     */
    public void deletePaymentProcessor(final long id) {
        PaymentProcessor paymentProcessorToDelete = getPaymentProcessorById(id);

        paymentProcessorRepository.delete(paymentProcessorToDelete);
    }

    /**
     * Get all payment processor objects by the entered ids
     * 
     * @param paymentProcessorIds
     *            list of payment processor ids that we need to find
     * @return list of payment processors
     * @throws CustomBadRequestException
     *             when at least one id does not exist
     */
    public List<PaymentProcessor> getPaymentProcessorsByIds(Set<Long> paymentProcessorIds) {
        List<PaymentProcessor> result = paymentProcessorRepository.findAll(paymentProcessorIds);

        if (result.size() == paymentProcessorIds.size()) {
            return result;
        }

        // Create a detail error
        if (result == null || result.isEmpty()) {
            throw new CustomBadRequestException(
                    "The following payment processors don't exist.  List = [" + paymentProcessorIds + "]");
        }

        Set<Long> paymentProcessorsNotFound = paymentProcessorIds.stream().filter(x -> !result.stream()
                .map(PaymentProcessor::getPaymentProcessorId).collect(Collectors.toSet()).contains(x))
                .collect(Collectors.toSet());

        throw new CustomBadRequestException(
                "The following payment processors don't exist.  List = [" + paymentProcessorsNotFound + "]");
    }

    private boolean existPaymentProcessorName(String processorName) {
        return paymentProcessorRepository.getPaymentProcessorByProcessorName(processorName) == null ? false : true;
    }
}
