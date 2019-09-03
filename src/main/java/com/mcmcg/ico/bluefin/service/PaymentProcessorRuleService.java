package com.mcmcg.ico.bluefin.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRuleDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorThresholdDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorRuleResource;
import com.mcmcg.ico.bluefin.rest.resource.ProcessRuleResource;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

@Service
@Transactional
public class PaymentProcessorRuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleService.class);

    @Autowired
    private PaymentProcessorRuleDAO paymentProcessorRuleDAO;
    
    @Autowired
    private PaymentProcessorService paymentProcessorService;
    
    @Autowired
    private PaymentProcessorThresholdDAO paymentProcessorThresholdDAO;

    /**
     * Create new payment processor rule
     * 
     * @param paymentProcessorRule
     * @return
     */
    public PaymentProcessorRule createPaymentProcessorRule(final long processorId,
    		PaymentProcessorRule paymentProcessorRule) {
    	LOGGER.info("Entering to create Payment Processor Rule");
    	// Verify if payment processor exists
    	PaymentProcessor  loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(processorId);

    	// Payment processor must has merchants associate to it
    	if (!loadedPaymentProcessor.hasMerchantsAssociated()) {
    		LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Rule Creation Request", BluefinWebPortalConstants.SEPARATOR,
    				"Unable to create payment processor rule. Payment processor must have at least one merchant associated. Payment processor id : ", 
    				String.valueOf(loadedPaymentProcessor.getPaymentProcessorId())));
    		
    		throw new CustomNotFoundException(String.format(
    				"Unable to create payment processor rule.  Payment processor [%s] MUST has at least one merchant associated.",
    				loadedPaymentProcessor.getPaymentProcessorId()));
    	}

    	validatePaymentProcessorRule(paymentProcessorRule);

    	paymentProcessorRule.setPaymentProcessor(loadedPaymentProcessor);
    	paymentProcessorRule.setMonthToDateCumulativeAmount(BigDecimal.ZERO);
    	LOGGER.info("ready to save payment Processor Rule");
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
    public PaymentProcessorRule updatePaymentProcessorRule(PaymentProcessorRule paymentProcessorRule,
            long processorId) {

    	LOGGER.info("Entering to update Payment Processor Rule ");
    	PaymentProcessorRule paymentProcessorRuleToUpdate = getPaymentProcessorRule(
                paymentProcessorRule.getPaymentProcessorRuleId());

        // Verify if processor exists
    	PaymentProcessor loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(processorId);
    	LOGGER.debug("loadedPaymentProcessor ={} ",loadedPaymentProcessor);
       validatePaymentProcessorRule(paymentProcessorRule);

        // Update fields
        paymentProcessorRuleToUpdate.setCardType(paymentProcessorRule.getCardType());
   //     paymentProcessorRuleToUpdate.setMaximumMonthlyAmount(paymentProcessorRule.getMaximumMonthlyAmount());
        paymentProcessorRuleToUpdate
                .setNoMaximumMonthlyAmountFlag(paymentProcessorRule.getNoMaximumMonthlyAmountFlag());
        paymentProcessorRuleToUpdate.setPriority(paymentProcessorRule.getPriority());
        paymentProcessorRuleToUpdate.setPaymentProcessor(loadedPaymentProcessor);

        LOGGER.info("ready to update paymentProcessorRuleToUpdate");
        return paymentProcessorRuleDAO.updatepaymentProcessorRule(paymentProcessorRuleToUpdate);
    }

    /**
     * Get all payment processor rules
     * 
     * @return list of payment processor rules
     */
    public List<PaymentProcessorRule> getPaymentProcessorRules() {
        LOGGER.info("Getting all payment processor rules:");
        return paymentProcessorRuleDAO.findAll();
   	}

    /**
     * Get payment processor rule by id
     * 
     * @return payment processor rule
     * @throws CustomNotFoundException
     *             when payment processor rule doesn't exist
     */
    public PaymentProcessorRule getPaymentProcessorRule(final long id) {
		LOGGER.info("Entering to get Payment Processor Rule ");
    	PaymentProcessorRule paymentProcessorRule = paymentProcessorRuleDAO.findOne(id);
        if (paymentProcessorRule == null) {
        	LOGGER.error(LoggingUtil.adminAuditInfo("Unable to find payment processor rule with id : ", String.valueOf(id)));
        	
            throw new CustomNotFoundException(
                    String.format("Unable to find payment processor rule with id = [%s]", id));
        }

        LOGGER.debug("paymentProcessorRule ={} ",paymentProcessorRule);
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

		LOGGER.debug("loadedPaymentProcessor={} ",loadedPaymentProcessor);
    	List<PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
    			.findByPaymentProcessor(loadedPaymentProcessor.getPaymentProcessorId());

    	LOGGER.debug("paymentProcessorRules ={} ",paymentProcessorRules);
    	return paymentProcessorRules == null ? new ArrayList<>(0) : paymentProcessorRules;
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
    	PaymentProcessorRule paymentProcessorRule = getPaymentProcessorRule(id);

    	LOGGER.debug("paymentProcessorRule ={} ",paymentProcessorRule);
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
    private void validatePaymentProcessorRule(PaymentProcessorRule newPaymentProcessorRule) {
		LOGGER.info("Entering to validate Payment Processor Rule : ");
        validatePaymentProcessorRuleForCreditDebitCardType(newPaymentProcessorRule);
    }

    private void validatePaymentProcessorRuleForCreditDebitCardType(PaymentProcessorRule newPaymentProcessorRule) {
        List<PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
                .findByCardType(newPaymentProcessorRule.getCardType().name());

        LOGGER.debug("PaymentProcessorRules size : {}",paymentProcessorRules.size());
        if (paymentProcessorRules == null || paymentProcessorRules.isEmpty()) {
            // No validation because there is no rules
            return;
        }

        // Record highest priority from the existing rules
        short highestPriority = 0;
        // Exist a no maximum amount limit
        boolean existsNoLimitPaymentProcessorRule = false;

        for (PaymentProcessorRule current : paymentProcessorRules) {
            if (newPaymentProcessorRule.getPaymentProcessorRuleId() == null || !current.getPaymentProcessorRuleId()
                    .equals(newPaymentProcessorRule.getPaymentProcessorRuleId())) {
                // Priority already assigned for the same transaction type
            	validatePriority(current,newPaymentProcessorRule);

                // Do not allow adding or editing if already exist a no
                // maximum monthly amount
            	validateNoLimit(current,newPaymentProcessorRule);

                // Find the highest priority of the existing rules
                highestPriority = highestPriority > current.getPriority().shortValue() ? highestPriority
                        : current.getPriority().shortValue();

                // There is a no maximum monthly amount ?
                if (current.hasNoLimit()) {
                    existsNoLimitPaymentProcessorRule = true;
                }
            }
        }

        validatePriorityAndHighestPriority(newPaymentProcessorRule,highestPriority,existsNoLimitPaymentProcessorRule);
        
        validatePriorityAndHighestPriority(newPaymentProcessorRule,highestPriority);
        
    }
    
    private void validatePriority(PaymentProcessorRule current,PaymentProcessorRule newPaymentProcessorRule){
    	if (current.getPriority().equals(newPaymentProcessorRule.getPriority())) {
            LOGGER.error(
                    "Unable to create/update payment processor rule with an existing priority.  Details: [{}]",
                    newPaymentProcessorRule);
            throw new CustomBadRequestException(
                    "Unable to create/update payment processor rule with an existing priority.");
        }
    }
    
    private void validateNoLimit(PaymentProcessorRule current,PaymentProcessorRule newPaymentProcessorRule){
    	if (newPaymentProcessorRule.hasNoLimit() && current.hasNoLimit()) {
            LOGGER.error(
                    "Unable to create/update payment processor rule with no maximum amount because already exist one.  Details: [{}]",
                    newPaymentProcessorRule);
            throw new CustomBadRequestException(
                    "Unable to create/update payment processor rule with no maximum amount because already exist one.");
        }
    }
    private void validatePriorityAndHighestPriority(PaymentProcessorRule newPaymentProcessorRule,short highestPriority,boolean existsNoLimitPaymentProcessorRule){
    	/*
         * When the new payment processor rule has noMaximumMonthlyAmountFlag ON
         * then we need to make sure that has the lowest priority (which means
         * that MUST has the highest number)
         */
        if (existsNoLimitPaymentProcessorRule && newPaymentProcessorRule.getPriority().shortValue() > highestPriority) {
            LOGGER.error(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.  Details: [{}]",
                    newPaymentProcessorRule);
            throw new CustomBadRequestException(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.");
        }
    }
    
    private void validatePriorityAndHighestPriority(PaymentProcessorRule newPaymentProcessorRule,short highestPriority){
    	if (newPaymentProcessorRule.hasNoLimit()
                && newPaymentProcessorRule.getPriority().shortValue() < highestPriority) {
            LOGGER.error(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.  Details: [{}]",
                    newPaymentProcessorRule);
            throw new CustomBadRequestException(
                    "Unable to create payment processor rule with no maximum amount because the priority is not the lowest value.");
        }
    }
    
    public void validatePaymentProcessorRuleData(PaymentProcessorRuleResource paymentProcessorRuleResource){
    	validateprocessorRuleInputData(paymentProcessorRuleResource);
    	validateCardTypeWithTargetPercentage(paymentProcessorRuleResource);
    	validateMaxMonthlyAmountAndNoFlag(paymentProcessorRuleResource);
    }
    
    private void validateprocessorRuleInputData(PaymentProcessorRuleResource paymentProcessorRuleResource){
    	PaymentProcessorRule paymentProcessorRule = null;
    	BigDecimal consumedPercentage;
    	BigDecimal oldTargetPercentage;
    	BigDecimal hundred = new BigDecimal(100);
    	
    	for(ProcessRuleResource processRuleResource : paymentProcessorRuleResource.getProcessRuleResource()) {
    		if(processRuleResource.getPaymentProcessorRuleIdDelete() != 1) {
    			BigDecimal targetPercentage = processRuleResource.getTargetPercentage();
            	
            	Long paymentProcessorId = processRuleResource.getPaymentProcessorId();
            	if(paymentProcessorId == null || paymentProcessorId<=0) {
            		throw new CustomBadRequestException("The payment processor cannot be blank");
            	}
            	if(StringUtils.isBlank(processRuleResource.getCardType().toString())) {
            		throw new CustomBadRequestException("The card type cannot be blank");
            	}
            	if((targetPercentage.compareTo(BigDecimal.ONE)) < 1 || (targetPercentage.compareTo(hundred)>100)) {
            		throw new CustomBadRequestException("Target percentage limit must exists in between 1 to 100");
            	}
            	if((processRuleResource.getMaximumMonthlyAmount()).compareTo(BigDecimal.ZERO) == -1) {
            		throw new CustomBadRequestException("Monthly maximum amount can't be less than zero");
            	}
            	if((processRuleResource.getNoMaximumMonthlyAmountFlag())!=0 || (processRuleResource.getNoMaximumMonthlyAmountFlag())!=1) {
            		throw new CustomBadRequestException("No limit flag can't be other than 0 or 1");
            	}
            	
    			if(processRuleResource.getPaymentProcessorRuleId()!=null && processRuleResource.getPaymentProcessorRuleId()>0) {
    				paymentProcessorRule = getPaymentProcessorRule(processRuleResource.getPaymentProcessorRuleId());
    				
    				BigDecimal newTargetPercentage = processRuleResource.getTargetPercentage();
    				
    				consumedPercentage = paymentProcessorRule.getConsumedPercentage();
    				oldTargetPercentage = paymentProcessorRule.getTargetPercentage();
                	BigDecimal newPercentageFactor = oldTargetPercentage.divide(hundred,3, BigDecimal.ROUND_UNNECESSARY);
                	BigDecimal consumedTargetPercentage = consumedPercentage.multiply(newPercentageFactor);
                	
                	int diffInAmount = newTargetPercentage.compareTo(consumedTargetPercentage);
    				if(diffInAmount<=0) {
    					throw new CustomBadRequestException("New target percentage" + newTargetPercentage + "can't be less or equal to consumed percentage "+consumedTargetPercentage);
    				}
    			}
    		}
    	}
    }
    
    private void validateMaxMonthlyAmountAndNoFlag(PaymentProcessorRuleResource paymentProcessorRuleResource){
    	BigDecimal monthlyMaxAmount;
    	
    	for(ProcessRuleResource processRuleResource : paymentProcessorRuleResource.getProcessRuleResource()) {
    		if(processRuleResource.getPaymentProcessorRuleIdDelete() != 1) {
    			monthlyMaxAmount = processRuleResource.getMaximumMonthlyAmount();
    			if(monthlyMaxAmount.compareTo(BigDecimal.ZERO) > 1 && processRuleResource.hasNoLimit()) {
    				throw new CustomBadRequestException("Maximum monthly amount and noLimit flag can't be select together");
    			}
    		}
    	} 
    }
    
    private void validateCardTypeWithTargetPercentage(PaymentProcessorRuleResource paymentProcessorRuleResource){
    	BigDecimal creditPercentageValue = new BigDecimal(0);
    	BigDecimal debitPercentageValue = new BigDecimal(0);
    	BigDecimal hundred = new BigDecimal(100);
    	
    	for(ProcessRuleResource processRuleResource : paymentProcessorRuleResource.getProcessRuleResource()) {
    		if(processRuleResource.getPaymentProcessorRuleIdDelete() != 1) {
    			BigDecimal targetPercentage = processRuleResource.getTargetPercentage();
            	if("DEBIT".equalsIgnoreCase(processRuleResource.getCardType().toString())) {
            		debitPercentageValue = debitPercentageValue.add(targetPercentage);
            	}
            	if("CREDIT".equalsIgnoreCase(processRuleResource.getCardType().toString())) {
            		creditPercentageValue = creditPercentageValue.add(targetPercentage);
            	}
        	} 
    	}
    	
        int totalDebitCardPercentage = debitPercentageValue.compareTo(hundred);
        int totalCreditCardPercentage = creditPercentageValue.compareTo(hundred);
        
        if(totalDebitCardPercentage != 0) {
        	throw new CustomBadRequestException("Sum of target percentage must equal to 100 for debit card type ");
        }
        
        if(totalCreditCardPercentage != 0) {
        	throw new CustomBadRequestException("Sum of target percentage must equal to 100 for credit card type ");
        }
    }
    
  /*  private PaymentProcessorThreshold handlePaymentProcessorThreshold(
			PaymentProcessorThreshold paymentProcessorThreshold) {
		if (paymentProcessorThreshold != null) {
			if (paymentProcessorThreshold.getPaymentProcessorThresholdId() != null) {
				return paymentProcessorThresholdDAO.save(paymentProcessorThreshold);
			} else {
				return paymentProcessorThresholdDAO.updatepaymentProcessorThreshold(paymentProcessorThreshold);
			}
		} else {
			throw new CustomBadRequestException("Unable to set credit/debit threshold.");
		}
	}*/
	
	private PaymentProcessorRule handlePaymentProcessorProcessorRuleActionCall(
			ProcessRuleResource processRuleResource) {
		PaymentProcessorRule ppr = processRuleResourceToPaymentProcessorRule(processRuleResource);
		// Verify if payment processor exists
    	PaymentProcessor  loadedPaymentProcessor = paymentProcessorService.getPaymentProcessorById(processRuleResource.getPaymentProcessorId());

    	// Payment processor must has merchants associate to it
    	if (!loadedPaymentProcessor.hasMerchantsAssociated()) {
    		LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Rule Creation Request", BluefinWebPortalConstants.SEPARATOR,
    				"Unable to create payment processor rule. Payment processor must have at least one merchant associated. Payment processor id : ", 
    				String.valueOf(loadedPaymentProcessor.getPaymentProcessorId())));
    		
    		throw new CustomNotFoundException(String.format(
    				"Unable to create payment processor rule.  Payment processor [%s] MUST has at least one merchant associated.",
    				loadedPaymentProcessor.getPaymentProcessorId()));
    	}
		if (processRuleResource.getPaymentProcessorRuleId()!=null && processRuleResource.getPaymentProcessorRuleId()!=0) {
			if (processRuleResource.getPaymentProcessorRuleIdDelete()==1) {
			//	return paymentProcessorThresholdDAO.save(paymentProcessorThreshold);
			} else {
			//	Code Block for Updating Existing Processor Rule.
				ppr.setPaymentProcessor(loadedPaymentProcessor);
				LOGGER.info("ready to update payment Processor Rule");
			  return  paymentProcessorRuleDAO.updatepaymentProcessorRule(ppr);	
			}
		} else {
		// Code Block for saving New Processor Rule.
			ppr.setPaymentProcessor(loadedPaymentProcessor);
			ppr.setMonthToDateCumulativeAmount(BigDecimal.ZERO);
			LOGGER.info("ready to save payment Processor Rule");
		  return  paymentProcessorRuleDAO.save(ppr);
		}
		return null;
	}
    
	/*private PaymentProcessorThreshold paymentProcessorRuleResourceToPaymentProcessorThreshold(
			PaymentProcessorRuleResource paymentProcessorRuleResource) {
		PaymentProcessorThreshold paymentProcessorThreshold = new PaymentProcessorThreshold();
		if (paymentProcessorRuleResource != null) {
			paymentProcessorThreshold
					.setCreditAmountThreshold(paymentProcessorRuleResource.getMaximumMonthlyAmountForCredit());
			paymentProcessorThreshold
					.setDebitAmountThreshold(paymentProcessorRuleResource.getMaximumMonthlyAmountForDebit());
		}
		return paymentProcessorThreshold;
	}*/
	
	public List<PaymentProcessorRule> createPaymentProcessorRuleConfig(
    		PaymentProcessorRuleResource paymentProcessorRuleResource,String userName) {
    	LOGGER.info("Entering to create Payment Processor Rule");
    	/*PaymentProcessorThreshold paymentProcessorThreshold=paymentProcessorRuleResourceToPaymentProcessorThreshold(paymentProcessorRuleResource);
    	paymentProcessorThreshold.setLastModifiedBy(userName);
    	PaymentProcessorThreshold insertedRecord=handlePaymentProcessorThreshold(paymentProcessorThreshold);*/
    	List<PaymentProcessorRule> paymentProcessorRuleList= new ArrayList<>();
		for (ProcessRuleResource prr : paymentProcessorRuleResource.getProcessRuleResource()) {
			paymentProcessorRuleList.add(handlePaymentProcessorProcessorRuleActionCall(prr));
		}
	//	insertedRecord.setPaymentProcessorRuleList(paymentProcessorRuleList);
    	return paymentProcessorRuleList;
    }

	//ProcessRuleResource  PaymentProcessorRule
	private PaymentProcessorRule processRuleResourceToPaymentProcessorRule(
			ProcessRuleResource processRuleResource) {
		PaymentProcessorRule paymentProcessorRule = new PaymentProcessorRule();
		if (processRuleResource != null) {
			paymentProcessorRule.setPaymentProcessorRuleId(processRuleResource.getPaymentProcessorRuleId());
			paymentProcessorRule.setTargetPercentage(processRuleResource.getTargetPercentage());
            paymentProcessorRule.setNoMaximumMonthlyAmountFlag(processRuleResource.getNoMaximumMonthlyAmountFlag());
            paymentProcessorRule.setMonthToDateCumulativeAmount(processRuleResource.getMonthToDateCumulativeAmount());
            paymentProcessorRule.setConsumedPercentage(processRuleResource.getConsumedPercentage());
            paymentProcessorRule.setCardType(processRuleResource.getCardType());
            paymentProcessorRule.setMaximumMonthlyAmount(processRuleResource.getMaximumMonthlyAmount());
		}
		return paymentProcessorRule;
	}
}
