package com.mcmcg.ico.bluefin.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRuleRepository;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRuleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class PaymentProcessorRuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleService.class);

    @Autowired
    private PaymentProcessorRuleRepository paymentProcessorRuleRepository;
    
    @Autowired
    private PaymentProcessorRuleDAO paymentProcessorRuleDAO;
    
    @Autowired
    private PaymentProcessorService paymentProcessorService;

    /**
     * Create new payment processor rule
     * 
     * @param paymentProcessorRule
     * @return
     */
    public com.mcmcg.ico.bluefin.model.PaymentProcessorRule createPaymentProcessorRule(final long processorId,
    		com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule) {
    	// Verify if payment processor exists
    	com.mcmcg.ico.bluefin.model.PaymentProcessor  loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(processorId);

    	// Payment processor must has merchants associate to it
    	if (!loadedPaymentProcessor.hasMerchantsAssociated()) {
    		LOGGER.error(
    				"Unable to create payment processor rule.  Payment processor MUST has at least one merchant associated.   Payment processor id = [{}]",
    				loadedPaymentProcessor.getPaymentProcessorId());
    		throw new CustomNotFoundException(String.format(
    				"Unable to create payment processor rule.  Payment processor [%s] MUST has at least one merchant associated.",
    				loadedPaymentProcessor.getPaymentProcessorId()));
    	}

    	//validatePaymentProcessorRule(paymentProcessorRule, loadedPaymentProcessor.getPaymentProcessorId());

    	paymentProcessorRule.setPaymentProcessor(loadedPaymentProcessor);
    	paymentProcessorRule.setMonthToDateCumulativeAmount(BigDecimal.ZERO);
    	return paymentProcessorRuleDAO.save(paymentProcessorRule);
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
    public com.mcmcg.ico.bluefin.model.PaymentProcessorRule updatePaymentProcessorRule(com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule,
            long processorId) {

    	com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRuleToUpdate = getPaymentProcessorRule(
                paymentProcessorRule.getPaymentProcessorRuleId());

        // Verify if processor exists
    	com.mcmcg.ico.bluefin.model.PaymentProcessor loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(processorId);
       // validatePaymentProcessorRule(paymentProcessorRule, loadedPaymentProcessor.getPaymentProcessorId());

        // Update fields
        paymentProcessorRuleToUpdate.setCardType(paymentProcessorRule.getCardType());
        paymentProcessorRuleToUpdate.setMaximumMonthlyAmount(paymentProcessorRule.getMaximumMonthlyAmount());
        paymentProcessorRuleToUpdate
                .setNoMaximumMonthlyAmountFlag(paymentProcessorRule.getNoMaximumMonthlyAmountFlag());
        paymentProcessorRuleToUpdate.setPriority(paymentProcessorRule.getPriority());
        paymentProcessorRuleToUpdate.setPaymentProcessor(loadedPaymentProcessor);

        return paymentProcessorRuleDAO.updatepaymentProcessorRule(paymentProcessorRuleToUpdate);
    }

    /**
     * Get all payment processor rules
     * 
     * @return list of payment processor rules
     */
    public List<PaymentProcessorRule> getPaymentProcessorRules() {
        LOGGER.info("Getting all payment processor rules");

        return paymentProcessorRuleRepository.findAll(new Sort(new Order(Direction.ASC, "cardType"),
                new Order(Direction.ASC, "priority"), new Order(Direction.ASC, "paymentProcessor")));
    }

    /**
     * Get payment processor rule by id
     * 
     * @return payment processor rule
     * @throws CustomNotFoundException
     *             when payment processor rule doesn't exist
     */
    public com.mcmcg.ico.bluefin.model.PaymentProcessorRule getPaymentProcessorRule(final long id) {
    	com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule = paymentProcessorRuleDAO.findOne(id);
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
    public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> getPaymentProcessorRulesByPaymentProcessorId(final long id) {
    	// Verify if processor exists
    	com.mcmcg.ico.bluefin.model.PaymentProcessor loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(id);

    	List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
    			.findByPaymentProcessor(loadedPaymentProcessor.getPaymentProcessorId());

    	return paymentProcessorRules == null ? new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(0) : paymentProcessorRules;
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
    	com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule = getPaymentProcessorRule(id);

    	paymentProcessorRuleDAO.delete(paymentProcessorRule.getPaymentProcessorRuleId());
    }

    public List<CardType> getTransactionTypes() {
        return Arrays.asList(CardType.values());
    }

    /**
     * Validate if the Payment Processor Rules has correct information
     * 
     * @param newPaymentProcessorRule,
     *            new rule to be created or updated (is update when the id is
     *            set)
     * @param paymentProcessorId,
     *            payment processor id
     */
    private void validatePaymentProcessorRule(com.mcmcg.ico.bluefin.model.PaymentProcessorRule newPaymentProcessorRule,
            final long paymentProcessorId) {
        validatePaymentProcessorRuleForCreditDebitCardType(newPaymentProcessorRule, paymentProcessorId);
    }

    @SuppressWarnings("unused")
    private void validatePaymentProcessorRuleForUnknownCardType(com.mcmcg.ico.bluefin.model.PaymentProcessorRule newPaymentProcessorRule,
            final long loadedPaymentProcessorId) {
        List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
                .findByCardType(newPaymentProcessorRule.getCardType().name());

        /*
         * Its impossible to have more than one payment processor rules with
         * UNKNOWN transaction type
         */
        if (paymentProcessorRules != null && paymentProcessorRules.size() > 1) {
            throw new CustomBadRequestException(
                    "Please verify payment processor rule table because there is more than one UNKNOWN transaction type.");
        }

        if (paymentProcessorRules.isEmpty()) {
            /*
             * Verify when doesn't exist one rule with UNKNOWN. ONLY create is
             * allowed
             */
            if (newPaymentProcessorRule.getPaymentProcessorRuleId() != null) {
                throw new CustomBadRequestException(
                        "Unable to create more than one payment processor rule with UNKNOWN transaction type.");
            }
        } else {
            /*
             * Verify when exist one rule with UNKNOWN. ONLY update is allowed
             * with same id
             */
        	com.mcmcg.ico.bluefin.model.PaymentProcessorRule loadedPaymentProcessorRule = paymentProcessorRules.get(0);

            if (newPaymentProcessorRule.getPaymentProcessorRuleId() == null || loadedPaymentProcessorRule
                    .getPaymentProcessorRuleId() != newPaymentProcessorRule.getPaymentProcessorRuleId()) {
                throw new CustomBadRequestException(
                        "Unable to create more than one payment processor rule with UNKNOWN transaction type.");
            }
        }

        // When maximumMonthlyAmount is NOT zero then throw an error
        if (newPaymentProcessorRule.getMaximumMonthlyAmount().compareTo(BigDecimal.ZERO) != 0
                || newPaymentProcessorRule.getNoMaximumMonthlyAmountFlag().equals((short) 0)) {
            throw new CustomBadRequestException(
                    "Unable to create payment processor rule as UNKNOWN because MaximumMonthlyAmount must be zero and NoMaximumMonthlyAmountFlag must be 1.");
        }
    }

    private void validatePaymentProcessorRuleForCreditDebitCardType(com.mcmcg.ico.bluefin.model.PaymentProcessorRule newPaymentProcessorRule,
            final long loadedPaymentProcessorId) {
        List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
                .findByCardType(newPaymentProcessorRule.getCardType().name());

        if (paymentProcessorRules == null || paymentProcessorRules.isEmpty()) {
            // No validation because there is no rules
            return;
        }

        // Record highest priority from the existing rules
        short highestPriority = 0;
        // Exist a no maximum amount limit
        boolean existsNoLimitPaymentProcessorRule = false;

        for (com.mcmcg.ico.bluefin.model.PaymentProcessorRule current : paymentProcessorRules) {
            if (newPaymentProcessorRule.getPaymentProcessorRuleId() == null || !current.getPaymentProcessorRuleId()
                    .equals(newPaymentProcessorRule.getPaymentProcessorRuleId())) {
                // Priority already assigned for the same transaction type
                if (current.getPriority().equals(newPaymentProcessorRule.getPriority())) {
                    LOGGER.error(
                            "Unable to create/update payment processor rule with an existing priority.  Details: [{}]",
                            newPaymentProcessorRule.toString());
                    throw new CustomBadRequestException(
                            "Unable to create/update payment processor rule with an existing priority.");
                }

                // Do not allow adding or editing if already exist a no
                // maximum monthly amount
                if (newPaymentProcessorRule.hasNoLimit() && current.hasNoLimit()) {
                    LOGGER.error(
                            "Unable to create/update payment processor rule with no maximum amount because already exist one.  Details: [{}]",
                            newPaymentProcessorRule.toString());
                    throw new CustomBadRequestException(
                            "Unable to create/update payment processor rule with no maximum amount because already exist one.");
                }

                // Find the highest priority of the existing rules
                highestPriority = highestPriority > current.getPriority().shortValue() ? highestPriority
                        : current.getPriority().shortValue();

                // There is a no maximum monthly amount ?
                if (current.hasNoLimit()) {
                    existsNoLimitPaymentProcessorRule = true;
                }
            }
        }

        /*
         * When the new payment processor rule has noMaximumMonthlyAmountFlag ON
         * then we need to make sure that has the lowest priority (which means
         * that MUST has the highest number)
         */
        if (existsNoLimitPaymentProcessorRule && newPaymentProcessorRule.getPriority().shortValue() > highestPriority) {
            LOGGER.error(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.  Details: [{}]",
                    newPaymentProcessorRule.toString());
            throw new CustomBadRequestException(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.");
        }

        if (newPaymentProcessorRule.hasNoLimit()
                && newPaymentProcessorRule.getPriority().shortValue() < highestPriority) {
            LOGGER.error(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.  Details: [{}]",
                    newPaymentProcessorRule.toString());
            throw new CustomBadRequestException(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.");
        }
    }
}
