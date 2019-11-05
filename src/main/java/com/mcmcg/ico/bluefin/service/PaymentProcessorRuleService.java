package com.mcmcg.ico.bluefin.service;

import java.math.BigDecimal;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRuleDateWiseTrends;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRuleTrends;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRuleTrendsRequest;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRuleDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRuleTrendsDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
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
    private PaymentProcessorRuleTrendsDAO paymentProcessorRuleTrendsDAO;

    /**
     * Create new payment processor rule
     * 
     * @param paymentProcessorRule
     * @return
     */
    
    
    
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
    	Map<Long, String> processorWithCardTypeMap = new HashMap<>();
    	
    	for(ProcessRuleResource processRuleResource : paymentProcessorRuleResource.getProcessRuleResource()) {
    		PaymentProcessor loadedPaymentProcessor = paymentProcessorService
					.getPaymentProcessorById(processRuleResource.getPaymentProcessorId());
    		if(processRuleResource.getIsRuleDeleted()<0 || processRuleResource.getIsRuleDeleted()>1) {
        		throw new CustomBadRequestException("Delete flag can't be other than 0 or 1");
        	}
    		if(processRuleResource.getIsRuleActive()<0 || processRuleResource.getIsRuleActive()>1) {
        		throw new CustomBadRequestException("Active flag can't be other than 0 or 1");
        	}
    		
    		if(processRuleResource.getIsRuleDeleted() != 1) {
    			
				if ((!processorWithCardTypeMap.isEmpty())
						&& StringUtils
								.isNotBlank(processorWithCardTypeMap.get(processRuleResource.getPaymentProcessorId()))
						&& processorWithCardTypeMap.get(processRuleResource.getPaymentProcessorId())
								.equalsIgnoreCase(processRuleResource.getCardType().toString())) {
					
					throw new CustomBadRequestException(
							"Payment processor rule can't be created for same payment processor "
									+ loadedPaymentProcessor.getProcessorName() + "  and " + processRuleResource.getCardType()
									+ " card type");
				}
				processorWithCardTypeMap.put(processRuleResource.getPaymentProcessorId(), processRuleResource.getCardType().toString());
    			
    			if(processRuleResource.getIsRuleActive() != 0) {
    				BigDecimal targetPercentage = processRuleResource.getTargetPercentage();
                	
                	Long paymentProcessorId = processRuleResource.getPaymentProcessorId();
                	if(paymentProcessorId == null || paymentProcessorId<=0) {
                		throw new CustomBadRequestException("The payment processor cannot be blank");
                	}
                	if(StringUtils.isBlank(processRuleResource.getCardType().toString())) {
                		throw new CustomBadRequestException("The card type cannot be blank");
                	}
                	if((targetPercentage.compareTo(BigDecimal.ONE)) < 0 || (targetPercentage.compareTo(hundred) > 0)) {
                		throw new CustomBadRequestException("Target percentage limit must exists in between 1 to 100");
                	}
                	if((processRuleResource.getMaximumMonthlyAmount()).compareTo(BigDecimal.ZERO) == -1) {
                		throw new CustomBadRequestException("Monthly maximum amount can't be less than zero");
                	}
                	if((processRuleResource.getNoMaximumMonthlyAmountFlag())<0 || (processRuleResource.getNoMaximumMonthlyAmountFlag())>1) {
                		throw new CustomBadRequestException("No limit flag can't be other than 0 or 1");
                	}
                	
                	processorWithCardTypeMap.put(processRuleResource.getPaymentProcessorId(), processRuleResource.getCardType().toString());
                	
        			/*if(processRuleResource.getPaymentProcessorRuleId()!=null && processRuleResource.getPaymentProcessorRuleId()>0) {
        				paymentProcessorRule = getPaymentProcessorRule(processRuleResource.getPaymentProcessorRuleId());
        				
        				BigDecimal newTargetPercentage = processRuleResource.getTargetPercentage();
        				
        				consumedPercentage = paymentProcessorRule.getConsumedPercentage();
        				oldTargetPercentage = paymentProcessorRule.getTargetPercentage();
                    	BigDecimal newPercentageFactor = oldTargetPercentage.divide(hundred,3, BigDecimal.ROUND_UNNECESSARY);
                    	BigDecimal consumedTargetPercentage = consumedPercentage.multiply(newPercentageFactor);
                    	
                    	int diffInAmount = newTargetPercentage.compareTo(consumedTargetPercentage);
						if (diffInAmount < 0) {
							throw new CustomBadRequestException("New target percentage [" + newTargetPercentage
									+ "] for " + loadedPaymentProcessor.getProcessorName() + " and "
									+ processRuleResource.getCardType()
									+ " cardtype can't be less or equal to consumed percentage ["
									+ consumedTargetPercentage + "]");
						}
        			}*/
    			}
    		}
    	}
    }
    
    private void validateMaxMonthlyAmountAndNoFlag(PaymentProcessorRuleResource paymentProcessorRuleResource){
    	BigDecimal monthlyMaxAmount;
    	
    	for(ProcessRuleResource processRuleResource : paymentProcessorRuleResource.getProcessRuleResource()) {
    		if(processRuleResource.getIsRuleDeleted() != 1 && processRuleResource.getIsRuleActive() != 0) {
    			monthlyMaxAmount = processRuleResource.getMaximumMonthlyAmount();
    			
    			if(monthlyMaxAmount.compareTo(BigDecimal.ZERO) == 0 && !processRuleResource.hasNoLimit()) {
    				throw new CustomBadRequestException("Select maximum monthly amount or noLimit flag to create payment processor rule");
    			}
    			if(monthlyMaxAmount.compareTo(BigDecimal.ZERO) > 0 && processRuleResource.hasNoLimit()) {
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
    		if(processRuleResource.getIsRuleDeleted() != 1 && processRuleResource.getIsRuleActive() != 0) {
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
    
	private PaymentProcessorRule handlePaymentProcessorProcessorRuleForInsertCall(
			ProcessRuleResource processRuleResource) {
		PaymentProcessorRule ppr = processRuleResourceToPaymentProcessorRule(processRuleResource);
		// Verify if payment processor exists
		PaymentProcessor loadedPaymentProcessor = paymentProcessorService
				.getPaymentProcessorById(processRuleResource.getPaymentProcessorId());

		// Payment processor must has merchants associate to it
		if (!loadedPaymentProcessor.hasMerchantsAssociated()) {
			LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Rule Creation Request",
					BluefinWebPortalConstants.SEPARATOR,
					"Unable to create payment processor rule. Payment processor must have at least one merchant associated. Payment processor id : ",
					String.valueOf(loadedPaymentProcessor.getPaymentProcessorId())));

			throw new CustomNotFoundException(String.format(
					"Unable to create payment processor rule.  Payment processor [%s] MUST has at least one merchant associated.",
					loadedPaymentProcessor.getPaymentProcessorId()));
		}
			ppr.setPaymentProcessor(loadedPaymentProcessor);
			ppr.setMonthToDateCumulativeAmount(BigDecimal.ZERO);
			LOGGER.debug("Ready to save payment Processor Rule");
			return paymentProcessorRuleDAO.save(ppr);
	}
	
	
	private PaymentProcessorRule handlePaymentProcessorProcessorRuleForUpdateCall(
			ProcessRuleResource processRuleResource) {
		PaymentProcessorRule ppr = processRuleResourceToPaymentProcessorRule(processRuleResource);
		// Verify if payment processor exists
		PaymentProcessor loadedPaymentProcessor = paymentProcessorService
				.getPaymentProcessorById(processRuleResource.getPaymentProcessorId());

		// Payment processor must has merchants associate to it
		if (!loadedPaymentProcessor.hasMerchantsAssociated()) {
			LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Rule Creation Request",
					BluefinWebPortalConstants.SEPARATOR,
					"Unable to create payment processor rule. Payment processor must have at least one merchant associated. Payment processor id : ",
					String.valueOf(loadedPaymentProcessor.getPaymentProcessorId())));

			throw new CustomNotFoundException(String.format(
					"Unable to create payment processor rule.  Payment processor [%s] MUST has at least one merchant associated.",
					loadedPaymentProcessor.getPaymentProcessorId()));
		}
		ppr.setPaymentProcessor(loadedPaymentProcessor);
		LOGGER.debug("Ready to update payment Processor Rule");
		return paymentProcessorRuleDAO.updatepaymentProcessorRule(ppr);

	}
    
	public List<PaymentProcessorRule> createPaymentProcessorRuleConfig(
			PaymentProcessorRuleResource paymentProcessorRuleResource, String userName) {
		LOGGER.debug("Grouping of Rules depanding upon their operations");
		List<ProcessRuleResource> paymentProcessorRuleListForSave = new ArrayList<>();
		List<ProcessRuleResource> paymentProcessorRuleListForUpdate = new ArrayList<>();
		List<ProcessRuleResource> paymentProcessorRuleListForDelete = new ArrayList<>();
		for (ProcessRuleResource processRuleResource : paymentProcessorRuleResource.getProcessRuleResource()) {
			if (processRuleResource.getIsRuleDeleted() == 1) {
				paymentProcessorRuleListForDelete.add(processRuleResource);
			} else if (processRuleResource.getIsRuleDeleted() != 1
					&& processRuleResource.getPaymentProcessorRuleId() == 0) {
				paymentProcessorRuleListForSave.add(processRuleResource);
			} else if (processRuleResource.getIsRuleDeleted() != 1
					&& processRuleResource.getPaymentProcessorRuleId() != 0) {
				paymentProcessorRuleListForUpdate.add(processRuleResource);
			}
		}
		for (ProcessRuleResource recordForDelete : paymentProcessorRuleListForDelete) {
			LOGGER.debug("Ready to delete payment Processor Rule with {} ruleid",
					recordForDelete.getPaymentProcessorRuleId());
			if (recordForDelete != null && recordForDelete.getPaymentProcessorRuleId() != null) {
				paymentProcessorRuleDAO.delete(recordForDelete.getPaymentProcessorRuleId());
			}
		}
		for (ProcessRuleResource recordForUpdate : paymentProcessorRuleListForUpdate) {
			LOGGER.debug("Ready to Update payment Processor Rule with {} ruleid",
					recordForUpdate.getPaymentProcessorRuleId());
			if (recordForUpdate != null) {
				handlePaymentProcessorProcessorRuleForUpdateCall(recordForUpdate);
			}
		}
		for (ProcessRuleResource recordForInsert : paymentProcessorRuleListForSave) {
			LOGGER.debug("Ready to create new payment processor rule with processor id is {}, target percentage is {}",
					recordForInsert.getPaymentProcessorId(), recordForInsert.getTargetPercentage());
			if (recordForInsert != null) {
				handlePaymentProcessorProcessorRuleForInsertCall(recordForInsert);
			}
		}
		return getPaymentProcessorRules();
	}

	private PaymentProcessorRule processRuleResourceToPaymentProcessorRule(
			ProcessRuleResource processRuleResource) {
		PaymentProcessorRule paymentProcessorRule = new PaymentProcessorRule();
		if (processRuleResource != null && processRuleResource.getIsRuleDeleted()!=1) {
			paymentProcessorRule.setPaymentProcessorRuleId(processRuleResource.getPaymentProcessorRuleId());
			paymentProcessorRule.setTargetPercentage(processRuleResource.getTargetPercentage());
            paymentProcessorRule.setNoMaximumMonthlyAmountFlag(processRuleResource.getNoMaximumMonthlyAmountFlag());
            paymentProcessorRule.setMonthToDateCumulativeAmount(processRuleResource.getMonthToDateCumulativeAmount());
            paymentProcessorRule.setConsumedPercentage(processRuleResource.getConsumedPercentage());
            paymentProcessorRule.setCardType(processRuleResource.getCardType());
            paymentProcessorRule.setMaximumMonthlyAmount(processRuleResource.getMaximumMonthlyAmount());
            paymentProcessorRule.setIsRuleActive(processRuleResource.getIsRuleActive());
		}
		if(processRuleResource != null && processRuleResource.getIsRuleDeleted()==1){
			paymentProcessorRule.setPaymentProcessorRuleId(processRuleResource.getPaymentProcessorRuleId());
		}
		return paymentProcessorRule;
	}
	
	public PaymentProcessorRuleTrends getProcessorRuleTrendsListByFrequency(
			String startDate,String endDate,String frequency) {
		LOGGER.info("Inside Service method");
		PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest =new PaymentProcessorRuleTrendsRequest();
		paymentProcessorRuleTrendsRequest.setStartDate(startDate);
		paymentProcessorRuleTrendsRequest.setEndDate(endDate);
		paymentProcessorRuleTrendsRequest.setFrequencyType(frequency);
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		Format format = new SimpleDateFormat("yyyy-MM-dd");
		validationForDate(paymentProcessorRuleTrendsRequest, formatter);
		resetingTimeForStartAndEndDate(paymentProcessorRuleTrendsRequest, formatter, format);
		paymentProcessorRuleTrendsRequest = getDateFilterByFrequency(paymentProcessorRuleTrendsRequest, formatter,format);
		List<PaymentProcessorRule> list = paymentProcessorRuleTrendsDAO
				.getTrendsByFrequencyandDateRange(paymentProcessorRuleTrendsRequest);
		PaymentProcessorRuleTrends processorRuleTrends = new PaymentProcessorRuleTrends();
		List<PaymentProcessorRuleDateWiseTrends> processorruleDatewiseTrendsList = new ArrayList<>();
		List<PaymentProcessorRule> processorRuleTrendsList = new ArrayList<>();
		PaymentProcessorRuleDateWiseTrends paymentProcessorRuleDateWiseTrends;
		DateTime histroyDate = null;
		for (PaymentProcessorRule lst : list) {
			if (histroyDate == null) {
				histroyDate = lst.getHistoryCreationDate();
			}
			if (histroyDate.equals(lst.getHistoryCreationDate())) {
				processorRuleTrendsList.add(lst);
			} else {
				paymentProcessorRuleDateWiseTrends = new PaymentProcessorRuleDateWiseTrends();
				paymentProcessorRuleDateWiseTrends.setPaymentProcessorRule(processorRuleTrendsList);
				paymentProcessorRuleDateWiseTrends.setHistroyDateCreation(histroyDate);
				if(histroyDate!=null){
				manageTrendsDateHeaderByFrequency(paymentProcessorRuleDateWiseTrends, paymentProcessorRuleTrendsRequest,
						format);
				}
				processorruleDatewiseTrendsList.add(paymentProcessorRuleDateWiseTrends);
				histroyDate = lst.getHistoryCreationDate();
				processorRuleTrendsList = new ArrayList<>();
				processorRuleTrendsList.add(lst);
			}

		}
		paymentProcessorRuleDateWiseTrends = new PaymentProcessorRuleDateWiseTrends();
		paymentProcessorRuleDateWiseTrends.setPaymentProcessorRule(processorRuleTrendsList);
		paymentProcessorRuleDateWiseTrends.setHistroyDateCreation(histroyDate);
		if(histroyDate!=null){
			manageTrendsDateHeaderByFrequency(paymentProcessorRuleDateWiseTrends, paymentProcessorRuleTrendsRequest,
					format);
			}
		processorruleDatewiseTrendsList.add(paymentProcessorRuleDateWiseTrends);
		processorRuleTrends.setPaymentProcessorRuleDateWiseTrends(processorruleDatewiseTrendsList);
		processorRuleTrends.setFrequencyType(paymentProcessorRuleTrendsRequest.getFrequencyType());
		return processorRuleTrends;
	}

	public PaymentProcessorRuleTrendsRequest getDateFilterByFrequency(
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest, DateTimeFormatter formatter,
			Format format) {

		LOGGER.info("Setting StartDate for {} frequency type", paymentProcessorRuleTrendsRequest.getFrequencyType());
		switch (StringUtils.isNotBlank(paymentProcessorRuleTrendsRequest.getFrequencyType())
				? paymentProcessorRuleTrendsRequest.getFrequencyType().toUpperCase()
				: paymentProcessorRuleTrendsRequest.getFrequencyType()) {
		case "DAILY":
			paymentProcessorRuleTrendsRequest.setFrequencyType("DAILY");
			break;
		case "WEEKLY":
			manageStartDateforWeeklyFrequency(paymentProcessorRuleTrendsRequest, formatter, format);
			paymentProcessorRuleTrendsRequest.setFrequencyType("WEEKLY");
			break;
		case "MONTHLY":
			manageStartDateforMonthlyFrequency(paymentProcessorRuleTrendsRequest, formatter, format);
			paymentProcessorRuleTrendsRequest.setFrequencyType("MONTHLY");
			break;
		default:
			manageStartDateforMonthlyFrequency(paymentProcessorRuleTrendsRequest, formatter, format);
			paymentProcessorRuleTrendsRequest.setFrequencyType("MONTHLY");
			break;
		}
		return paymentProcessorRuleTrendsRequest;
	}
    
	private PaymentProcessorRuleTrendsRequest resetingTimeForStartAndEndDate(
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest, DateTimeFormatter formatter,
			Format format) {
		DateTime dateTime = null;
		Date startDate = null;
		Date endDate = null;
		dateTime = formatter.parseDateTime(paymentProcessorRuleTrendsRequest.getStartDate());
		startDate = dateTime.toDate();
		paymentProcessorRuleTrendsRequest.setStartDate(format.format(startDate));
		dateTime = formatter.parseDateTime(paymentProcessorRuleTrendsRequest.getEndDate());
		dateTime= dateTime.plusDays(1);
		endDate = dateTime.toDate();
		if(!endDate.after(startDate)){
			throw new CustomException("Start Date cannot be greater than End Date");
		}
		paymentProcessorRuleTrendsRequest.setEndDate(format.format(endDate));
		return paymentProcessorRuleTrendsRequest;
	}
	
	private PaymentProcessorRuleTrendsRequest manageStartDateforMonthlyFrequency(
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest, DateTimeFormatter formatter,
			Format format) {
		DateTime dateTime = null;
		Date startDate = null;
		dateTime = formatter.parseDateTime(paymentProcessorRuleTrendsRequest.getStartDate());
		dateTime = dateTime.withDayOfMonth(1);
		startDate = dateTime.toDate();
		paymentProcessorRuleTrendsRequest.setStartDate(format.format(startDate));
		return paymentProcessorRuleTrendsRequest;
	}
	
	private PaymentProcessorRuleTrendsRequest manageStartDateforWeeklyFrequency(
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest, DateTimeFormatter formatter,
			Format format) {
		DateTime dateTime = null;
		Date startDate = null;
		dateTime = formatter.parseDateTime(paymentProcessorRuleTrendsRequest.getStartDate());
		dateTime = dateTime.withDayOfWeek(DateTimeConstants.MONDAY);
		startDate = dateTime.toDate();
		paymentProcessorRuleTrendsRequest.setStartDate(format.format(startDate));
		return paymentProcessorRuleTrendsRequest;
	}
	
	private void validationForDate(PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest,
			DateTimeFormatter formatter) {
		LOGGER.info("Inside Validation code for start and end date ");
		try {
			formatter.parseDateTime(paymentProcessorRuleTrendsRequest.getStartDate());
		} catch (IllegalArgumentException e) {
			throw new CustomException("Invalid Format of Start Date");
		}
		try {
			formatter.parseDateTime(paymentProcessorRuleTrendsRequest.getEndDate());
		} catch (IllegalArgumentException e) {
			throw new CustomException("Invalid Format of End Date");
		}
	}
	
	private void setTrendsDateHeaderForWeeklyFrequency(PaymentProcessorRuleDateWiseTrends paymentProcessorRuleDateWiseTrends,
			 Format format) {
		LOGGER.info("Setting Trends Date Header for Weekly Frequency");
		DateTime dateTime = null;
		Date startDate = null;
		Date endDate = null;
		String weeklyDateHeader;
		dateTime = paymentProcessorRuleDateWiseTrends.getHistroyDateCreation().withDayOfWeek(DateTimeConstants.MONDAY);
		startDate = dateTime.toDate();
		dateTime = paymentProcessorRuleDateWiseTrends.getHistroyDateCreation().withDayOfWeek(DateTimeConstants.SUNDAY);
		endDate = dateTime.toDate();
		weeklyDateHeader= format.format(startDate)  + " - " + format.format(endDate);
		paymentProcessorRuleDateWiseTrends.setTrendsDateHeader(weeklyDateHeader);
	}
	
	private void setTrendsDateHeaderForMonthlyFrequency(
			PaymentProcessorRuleDateWiseTrends paymentProcessorRuleDateWiseTrends) {
		LOGGER.info("Setting Trends Date Header for Monthly Frequency");
		DateTime dateTime = null;
		Date startDate = null;
		Format formatter = new SimpleDateFormat("MMM,yyy");
		String dateHeader;
		dateTime = paymentProcessorRuleDateWiseTrends.getHistroyDateCreation();
		startDate = dateTime.toDate();
		dateHeader = formatter.format(startDate);
		paymentProcessorRuleDateWiseTrends.setTrendsDateHeader(dateHeader);
	}
	
	private void setTrendsDateHeaderForDailyFrequency(PaymentProcessorRuleDateWiseTrends paymentProcessorRuleDateWiseTrends,
			 Format format) {
		LOGGER.info("Setting Trends Date Header for Daily Frequency");
		DateTime dateTime = null;
		Date startDate = null;
		String dateHeader;
		dateTime = paymentProcessorRuleDateWiseTrends.getHistroyDateCreation();
		startDate = dateTime.toDate();
		dateHeader =format.format(startDate);
		paymentProcessorRuleDateWiseTrends.setTrendsDateHeader(dateHeader);
	}
	
	private void manageTrendsDateHeaderByFrequency(
			PaymentProcessorRuleDateWiseTrends paymentProcessorRuleDateWiseTrends,
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest, Format format) {
		LOGGER.debug("Setting Trends Date Header for {} frequency type", paymentProcessorRuleTrendsRequest.getFrequencyType());
		switch (StringUtils.isNotBlank(paymentProcessorRuleTrendsRequest.getFrequencyType())
				? paymentProcessorRuleTrendsRequest.getFrequencyType().toUpperCase()
				: paymentProcessorRuleTrendsRequest.getFrequencyType()) {
		case "DAILY":
			setTrendsDateHeaderForDailyFrequency(paymentProcessorRuleDateWiseTrends, format);
			break;
		case "WEEKLY":
			setTrendsDateHeaderForWeeklyFrequency(paymentProcessorRuleDateWiseTrends, format);
			break;
		case "MONTHLY":
			setTrendsDateHeaderForMonthlyFrequency(paymentProcessorRuleDateWiseTrends);
			break;
		default:
			setTrendsDateHeaderForMonthlyFrequency(paymentProcessorRuleDateWiseTrends);
			break;
		}
	}
	
}
