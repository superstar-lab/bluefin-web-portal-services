package com.mcmcg.ico.bluefin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorResource;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

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
            LOGGER.error(String.format("Unable to find payment processor, it doesn't exists: %s", id));
            throw new CustomNotFoundException(
                    String.format("Unable to process request payment processor doesn't exists with given id: %s", id));
        }
        return paymentProcessor;
    }

    /**
     * This method will return a list of payment processors according to the
     * criteria given by parameter, if not match found a not found exception
     * will be thrown
     * 
     * @param exp
     * @param page
     * @param size
     * @param sort
     * @return List with payment processors that match the criteria given, not
     *         found exception if not match found
     */
    public Iterable<PaymentProcessor> getPaymentProcessors(BooleanExpression exp, Integer page, Integer size,
            String sort) {
        Page<PaymentProcessor> result = paymentProcessorRepository.findAll(exp,
                QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }
        return result;
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
        String processorName = paymentProcessor.getProcessorName();

        if (existPaymentProcessorName(processorName)) {
            LOGGER.error(String.format("Unable to create Payment Processor, this processor already exists: %s",
                    processorName));
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
            LOGGER.error(String.format("Unable to update payment processor, it doesn't exists: %s", id));
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
            LOGGER.error(String.format("Unable to delete payment processor, it doesn't exists: %s", id));
            throw new CustomNotFoundException(
                    String.format("Unable to process request payment processor doesn't exists with given id: %s", id));
        }
        paymentProcessorRepository.delete(paymentProcessorToDelete);
    }

    private boolean existPaymentProcessorName(String processorName) {
        return paymentProcessorRepository.getPaymentProcessorByProcessorName(processorName) == null ? false : true;
    }

}
