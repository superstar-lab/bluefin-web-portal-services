package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorResource;

@Service
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
    public PaymentProcessor getPaymentProcessorById(Long id) {
        PaymentProcessor paymentProcessor = paymentProcessorRepository.findOne(id);

        if (paymentProcessor == null) {
            LOGGER.error("Unable to find payment processor, it doesn't exists: [{}]", id);
            throw new CustomNotFoundException(
                    String.format("Unable to process request payment processor doesn't exists with given id: %s", id));
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
    public PaymentProcessor createPaymentProcessor(PaymentProcessorResource paymentProcessorResource) {
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
    public PaymentProcessor updatePaymentProcessor(Long id, PaymentProcessorResource paymentProcessorResource) {
        PaymentProcessor paymentProcessorToUpdate = paymentProcessorRepository.findOne(id);

        if (paymentProcessorToUpdate == null) {
            LOGGER.error("Unable to update payment processor, it doesn't exists: [{}]", id);
            throw new CustomNotFoundException(
                    String.format("Unable to process request payment processor doesn't exists with given id: %s", id));
        }

        paymentProcessorResource.updatePaymentProcessor(paymentProcessorToUpdate);
        return paymentProcessorRepository.save(paymentProcessorToUpdate);
    }

    /**
     * Deletes a payment processor by id, not found exception will be thrown if
     * payment processor does not exists
     * 
     * @param id
     */
    public void deletePaymentProcessor(Long id) {
        PaymentProcessor paymentProcessorToDelete = paymentProcessorRepository.findOne(id);

        if (paymentProcessorToDelete == null) {
            LOGGER.error("Unable to delete payment processor, it doesn't exists: [{}]", id);
            throw new CustomNotFoundException(
                    String.format("Unable to process request payment processor doesn't exists with given id: %s", id));
        }
        paymentProcessorRepository.delete(paymentProcessorToDelete);
    }

    private boolean existPaymentProcessorName(String processorName) {
        return paymentProcessorRepository.getPaymentProcessorByProcessorName(processorName) == null ? false : true;
    }

}
