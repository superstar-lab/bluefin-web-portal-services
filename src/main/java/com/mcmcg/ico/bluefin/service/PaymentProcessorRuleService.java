package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRuleRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class PaymentProcessorRuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleService.class);

    @Autowired
    private PaymentProcessorRuleRepository paymentProcessorRuleRepository;
    @Autowired
    private PaymentProcessorService paymentProcessorService;

    /**
     * Create new payment processor rule
     * 
     * @param paymentProcessorRule
     * @return
     */
    public PaymentProcessorRule createPaymentProcessorRule(PaymentProcessorRule paymentProcessorRule) {
        return paymentProcessorRuleRepository.save(paymentProcessorRule);
    }

    /**
     * Update existing payment processor rule
     * 
     * @param id
     *            payment processor rule id
     * @param paymentProcessorRule
     *            payment processor rule object with the information that must
     *            be updated
     * @return updated payment processor rule
     * @throws CustomNotFoundException
     *             when payment processor rule is not found
     */
    public PaymentProcessorRule updatePaymentProcessorRule(long id, PaymentProcessorRule paymentProcessorRule) {
        PaymentProcessorRule paymentProcessorRuleToUpdate = paymentProcessorRuleRepository.findOne(id);
        if (paymentProcessorRule == null) {
            throw new CustomNotFoundException(
                    String.format("Unable to find payment processor rule with id = [%s]", id));
        }

        // Update fields
        paymentProcessorRuleToUpdate.setPaymentProcessor(paymentProcessorRule.getPaymentProcessor());
        paymentProcessorRuleToUpdate.setCardType(paymentProcessorRule.getCardType());
        paymentProcessorRuleToUpdate.setMaximumMonthlyAmount(paymentProcessorRule.getMaximumMonthlyAmount());
        paymentProcessorRuleToUpdate
                .setNoMaximumMonthlyAmountFlag(paymentProcessorRule.getNoMaximumMonthlyAmountFlag());

        return paymentProcessorRuleRepository.save(paymentProcessorRuleToUpdate);
    }

    /**
     * Get all payment processor rules
     * 
     * @return list of payment processor rules
     */
    public List<PaymentProcessorRule> getPaymentProcessorRules() {
        LOGGER.info("Getting all payment processor rules");

        return paymentProcessorRuleRepository.findAll();
    }

    /**
     * Get payment processor rule by id
     * 
     * @return payment processor rule
     * @throws CustomNotFoundException
     *             when payment processor rule doesn't exist
     */
    public PaymentProcessorRule getPaymentProcessorRule(final long id) {
        PaymentProcessorRule paymentProcessorRule = paymentProcessorRuleRepository.findOne(id);
        if (paymentProcessorRule == null) {
            throw new CustomNotFoundException(
                    String.format("Unable to find payment processor rule with id = [%s]", id));
        }

        return paymentProcessorRule;
    }

    /**
     * Get payment processor rules by processor id
     * 
     * @return list of payment processor rules
     * @throws CustomNotFoundException
     *             when payment processor doesn't exist
     */
    public List<PaymentProcessorRule> getPaymentProcessorRulesByPaymentProcessorId(final long id) {
        // Verify if processor exists
        PaymentProcessor loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(id);
        if (loadedPaymentProcessor == null) {
            throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
        }

        List<PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleRepository
                .findByPaymentProcessor(loadedPaymentProcessor);

        return paymentProcessorRules == null ? new ArrayList<PaymentProcessorRule>(0) : paymentProcessorRules;
    }

    /**
     * Delete payment processor rule by id
     * 
     * @param id
     *            payment processor rule id
     * @throws CustomNotFoundException
     *             when payment processor rule doesn't exist
     */
    public void delete(final long id) {
        PaymentProcessorRule paymentProcessorRule = paymentProcessorRuleRepository.findOne(id);
        if (paymentProcessorRule == null) {
            throw new CustomNotFoundException(
                    String.format("Unable to find payment processor rule with id = [%s]", id));
        }

        paymentProcessorRuleRepository.delete(id);
    }
}
