package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorInternalResponseCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorInternalStatusCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorMerchantDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorResponseCodeDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRuleDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorStatusCodeDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.BasicPaymentProcessorResource;
import com.mcmcg.ico.bluefin.rest.resource.ItemStatusCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.ItemStatusResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorMerchantResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorStatusResource;

@Service
@Transactional
public class PaymentProcessorService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorService.class);

	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;

	@Autowired
	private PaymentProcessorRuleDAO paymentProcessorRuleDAO;

	@Autowired
	private PaymentProcessorMerchantDAO paymentProcessorMerchantDAO;

	@Autowired
	private PaymentProcessorCodeService paymentProcessorCodeService;

	@Autowired
	private PaymentProcessorInternalStatusCodeDAO paymentProcessorInternalStatusCodeDAO;
	
	@Autowired
	private PaymentProcessorInternalResponseCodeDAO paymentProcessorInternalResponseCodeDAO;
	
	@Autowired
	private PaymentProcessorResponseCodeDAO paymentProcessorResponseCodeDAO;

	@Autowired
	private PaymentProcessorStatusCodeDAO paymentProcessorStatusCodeDAO;

	/**
	 * This method will find a payment processor by its id, not found exception
	 * if it does not exist
	 * 
	 * @param id
	 * @return
	 */
	public PaymentProcessor getPaymentProcessorById(final long id) {
		PaymentProcessor paymentProcessor = paymentProcessorDAO
				.findByPaymentProcessorId(id);

		if (paymentProcessor == null) {
			throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
		}
		List<PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
				.findPaymentProccessorRulByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("paymentProcessorRules ={} ",paymentProcessorRules);
		paymentProcessor.setPaymentProcessorRules(paymentProcessorRules);

		List<PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
				.findPaymentProccessorMerchantByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("paymentProcessorMerchants ={} ",paymentProcessorMerchants);
		paymentProcessor.setPaymentProcessorMerchants(paymentProcessorMerchants);
		return paymentProcessor;
	}

	/**
	 * This method will return a list of all the payment processors
	 * 
	 * @return List with payment processors that match the criteria given, not
	 *         found exception if not match found
	 */
	public List<PaymentProcessor> getPaymentProcessors() {
		List<PaymentProcessor> result = paymentProcessorDAO.findAll();
		if (result != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("PaymentProcessor result size : {} ",result.size());
			}
			for (PaymentProcessor processor : result) {
				boolean isReadyToBeActivated = isReadyToBeActivated(processor.getPaymentProcessorId());
				if (processor.isActive() && !isReadyToBeActivated) {
					processor.setIsActive((short) 0);
					isReadyToBeActivated = false;
				}
				processor.setReadyToBeActivated(isReadyToBeActivated);
				List<PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
						.findPaymentProccessorRulByProcessorId(processor.getPaymentProcessorId());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("paymentProcessorRules size : {}",paymentProcessorRules.size());
				}
				processor.setPaymentProcessorRules(paymentProcessorRules);
	
				List<PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
						.findPaymentProccessorMerchantByProcessorId(processor.getPaymentProcessorId());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("paymentProcessorMerchants size : {} ",paymentProcessorMerchants.size());
				}
				processor.setPaymentProcessorMerchants(paymentProcessorMerchants);
			} 
			return result;
		}
		return new ArrayList<>();
	}

	/**
	 * This method will create a new Payment Processor that does not exist
	 * already, if name exists will throw a bad request exception
	 * 
	 * @param paymentProcessorResource
	 * @return Payment processor created
	 */
	public PaymentProcessor createPaymentProcessor(
			BasicPaymentProcessorResource paymentProcessorResource) {
		PaymentProcessor paymentProcessor = paymentProcessorResource.toPaymentProcessor();
		final String processorName = paymentProcessor.getProcessorName();

		LOGGER.debug("processorName ={} ",processorName);
		if (existPaymentProcessorName(processorName)) {
			LOGGER.error("Unable to create Payment Processor, this processor already exists: [{}]", processorName);
			throw new CustomBadRequestException(String
					.format("Unable to create Payment Processor, this processor already exists: %s", processorName));
		} else if (paymentProcessorResource.getIsActive() == 1) {
			LOGGER.error("Unable to create Payment Processor, new processor cannot be active: [{}]", processorName);
			throw new CustomBadRequestException(String
					.format("Unable to create Payment Processor, new processor cannot be active: %s", processorName));
		}

		LOGGER.info("ready to save  paymentProcessor : ");
		return paymentProcessorDAO.save(paymentProcessor);
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

		LOGGER.debug("paymentProcessorToUpdate ={} ",paymentProcessorToUpdate);
		if (paymentProcessorToUpdate.getIsActive() == 1 && !isReadyToBeActivated(id)) {
			paymentProcessorToUpdate.setIsActive((short) 0);
			paymentProcessorDAO.update(paymentProcessorToUpdate);
		}
		if (paymentProcessorResource.getIsActive() == 1 && !isReadyToBeActivated(id)) {
			LOGGER.error("Unable to activate Payment Processor, processor has some pending steps: [{}]",
					paymentProcessorToUpdate.getProcessorName());
			throw new CustomBadRequestException(
					String.format("Unable to activate Payment Processor, processor has some pending steps: %s",
							paymentProcessorToUpdate.getProcessorName()));
		}
		paymentProcessorToUpdate.setProcessorName(paymentProcessorResource.getProcessorName());
		paymentProcessorToUpdate.setRemitTransactionOpenTime(paymentProcessorResource.getRemitTransactionOpenTime());
		paymentProcessorToUpdate.setRemitTransactionCloseTime(paymentProcessorResource.getRemitTransactionCloseTime());
		paymentProcessorToUpdate.setIsActive(paymentProcessorResource.getIsActive());
		LOGGER.info("ready to update  paymentProcessorToUpdate  ");
		paymentProcessorDAO.update(paymentProcessorToUpdate);
		return paymentProcessorToUpdate;
	}

	public boolean isReadyToBeActivated(final long id) {
		PaymentProcessorStatusResource paymentProcessorStatus = getPaymentProcessorStatusById(id);
		LOGGER.debug("PaymentProcessorStatus : {} ",paymentProcessorStatus);
		boolean type1 = paymentProcessorStatus.getHasPaymentProcessorName().getCompleted()
				&& paymentProcessorStatus.getHasSameDayProcessing().getCompleted()
				&& paymentProcessorStatus.getHasMerchantsAssociated().getCompleted();
		boolean type2 = paymentProcessorStatus.getHasResponseCodesAssociated().getCompleted()
				&& paymentProcessorStatus.getHasRulesAssociated().getCompleted()
				&& paymentProcessorStatus.getHasStatusCodesAssociated().getCompleted();
		return type1 && type2 ;
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

		LOGGER.debug("paymentProcessorToUpdate : {}"
				,paymentProcessorToUpdate==null ? null : paymentProcessorToUpdate.getPaymentProcessorMerchants() == null ? null : paymentProcessorToUpdate.getPaymentProcessorMerchants().size());
		// User wants to clear payment processor merchants from payment
		// processor
		if (paymentProcessorMerchants.isEmpty()) {
			if (paymentProcessorToUpdate != null) {
				paymentProcessorToUpdate.getPaymentProcessorMerchants().clear();
				// Deleting PaymentProcessorMerchant from DB for specific payment
				// processor id.
				paymentProcessorMerchantDAO
					.deletPaymentProcessorMerchantByProcID(paymentProcessorToUpdate.getPaymentProcessorId()); 
			}
			return paymentProcessorToUpdate;
		} else {
			setPaymentProcessors(paymentProcessorToUpdate);
		}

		// New payment processor merchants that need to be created or updated
		Map<Long, PaymentProcessorMerchantResource> newMapOfPaymentProcessorMerchants = paymentProcessorMerchants
				.stream().collect(Collectors.toMap(
						PaymentProcessorMerchantResource::getLegalEntityAppId, p -> p));

		// Temporal list of legal entity app ids already updated
		Set<Long> paymentProcessorMerchantsToKeep = new HashSet<>();
		if (paymentProcessorToUpdate != null) {
			// Update information from current payment processor merchants
			Iterator<PaymentProcessorMerchant> iter = paymentProcessorToUpdate
					.getPaymentProcessorMerchants().iterator();
			while (iter.hasNext()) {
				PaymentProcessorMerchant element = iter.next();
	
				PaymentProcessorMerchantResource ppmr = newMapOfPaymentProcessorMerchants
						.get(element.getLegalEntityAppId());
				if(ppmr!= null) {
					element.setMerchantId(ppmr.getMerchantId());
					element.setTestOrProd(ppmr.getTestOrProd());
					paymentProcessorMerchantsToKeep.add(ppmr.getLegalEntityAppId());
				}
				
			}
		}

		// Add the new payment processor merchants
		addPaymentProcessorMerchants(paymentProcessorToUpdate,paymentProcessorMerchantsToKeep,newMapOfPaymentProcessorMerchants);
		
		finallyUpdatePaymentProcessor(paymentProcessorToUpdate);
		
		return paymentProcessorToUpdate;
	}
	
	private void setPaymentProcessors(PaymentProcessor paymentProcessorToUpdate){
		if (paymentProcessorToUpdate != null) {
			for (PaymentProcessorMerchant paymentProcessorMerchant : paymentProcessorToUpdate.getPaymentProcessorMerchants()) {
				PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(paymentProcessorToUpdate.getPaymentProcessorId());
				paymentProcessorMerchant.setPaymentProcessorId(paymentProcessor.getPaymentProcessorId());
			}
		}
	}

	private void addPaymentProcessorMerchants(PaymentProcessor paymentProcessorToUpdate,Set<Long> paymentProcessorMerchantsToKeep,Map<Long, PaymentProcessorMerchantResource> newMapOfPaymentProcessorMerchants){
		for (Entry<Long, PaymentProcessorMerchantResource> legalEntityEntry : newMapOfPaymentProcessorMerchants.entrySet()) {
			if (paymentProcessorToUpdate != null && !paymentProcessorMerchantsToKeep.contains(legalEntityEntry.getKey())) {
					paymentProcessorToUpdate.addPaymentProcessorMerchant(legalEntityEntry.getValue().toPaymentProcessorMerchant());
			}
		}
	}
	private void finallyUpdatePaymentProcessor(PaymentProcessor paymentProcessorToUpdate){
		if (paymentProcessorToUpdate != null) {
			paymentProcessorMerchantDAO
				.deletPaymentProcessorMerchantByProcID(paymentProcessorToUpdate.getPaymentProcessorId());
			paymentProcessorMerchantDAO
				.createPaymentProcessorMerchants(paymentProcessorToUpdate.getPaymentProcessorMerchants());
			LOGGER.debug("Exiting from updatePaymentProcessorMerchants ");
		}
	}
	/**
	 * Deletes a payment processor by id, not found exception will be thrown if
	 * payment processor does not exists
	 * 
	 * @param id
	 */
	public void deletePaymentProcessor(final long id) {

		PaymentProcessor paymentProcessorToDelete = getPaymentProcessorById(id);
		if (paymentProcessorToDelete != null) {
			LOGGER.debug("Payment processor {} record found to delete", paymentProcessorToDelete.getProcessorName());
			LOGGER.info("Payment processor internal status code deletion started");
			paymentProcessorInternalStatusCodeDAO.deletePaymentProcessorInternalStatusCodeForPaymentProcessor(id);
			LOGGER.info("Payment processor internal status code deletion completed");
			LOGGER.info("Payment processor status code deletion started");
			paymentProcessorStatusCodeDAO.deletePaymentProcessorStatusCode(id);
			LOGGER.info("Payment processor status code deletion completed");
			
			LOGGER.info("Payment processor internal response code deletion started");
			paymentProcessorInternalResponseCodeDAO.deletePaymentProcessorInternalResponseCodeForPaymentProcessor(id);
			LOGGER.info("Payment processor internal response code deletion completed");
			LOGGER.info("Payment processor response code deletion started");
			paymentProcessorResponseCodeDAO.deletePaymentProcessorResponseCode(id);
			LOGGER.info("Payment processor status code deletion completed");
			if (paymentProcessorToDelete.getPaymentProcessorRules() != null
					&& !paymentProcessorToDelete.getPaymentProcessorRules().isEmpty()) {
				LOGGER.info("Payment processor rules deletion started");
				paymentProcessorRuleDAO.deletePaymentProcessorRules(paymentProcessorToDelete.getPaymentProcessorId());
				LOGGER.info("Payment processor rules deletion completed");
			}
			if (paymentProcessorToDelete.getPaymentProcessorMerchants() != null
					&& !paymentProcessorToDelete.getPaymentProcessorMerchants().isEmpty()) {
				LOGGER.info("Payment processor merchants deletion started");
				paymentProcessorMerchantDAO
						.deletePaymentProcessorRules(paymentProcessorToDelete.getPaymentProcessorId());
				LOGGER.info("Payment processor merchants deletion completed");
			}
			/*
			 * Before deleting Status Code Need to delete
			 * PaymentProcessorInternalStatusCode
			 * paymentProcessorCodeService.deletePaymentProcessorStatusCode(
			 * paymentProcessorToDelete.getPaymentProcessorId());  Need
			 * to Delete PaymentProcessorResponseCode and it is also dependent
			 * on PaymentProcessorInternalRespnseCode.
			 */
			paymentProcessorDAO.delete(paymentProcessorToDelete);
		}
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
		LOGGER.info("Entering to get PaymentProcessorsByIds ");
		List<PaymentProcessor> result = paymentProcessorDAO.findAll(paymentProcessorIds);

		if (result != null && result.size() == paymentProcessorIds.size()) {
			return result;
		}

		// Create a detail error
		if (result == null || result.isEmpty() ) {
			throw new CustomBadRequestException(
					"The following payment processors don't exist.  List = [" + paymentProcessorIds + "]");
		}

		Set<Long> paymentProcessorsNotFound = paymentProcessorIds.stream()
				.filter(x -> !result.stream().map(PaymentProcessor::getPaymentProcessorId)
						.collect(Collectors.toSet()).contains(x))
				.collect(Collectors.toSet());

		throw new CustomBadRequestException(
				"The following payment processors don't exist.  List = [" + paymentProcessorsNotFound + "]");
	}

	private boolean existPaymentProcessorName(String processorName) {
		LOGGER.info("existPaymentProcessorName ");
		return paymentProcessorDAO.getPaymentProcessorByProcessorName(processorName) == null ? false : true;
	}

	/**
	 * This method will find a payment processor status by its id, not found
	 * exception if it does not exist
	 * 
	 * @param id
	 * @return
	 */
	public PaymentProcessorStatusResource getPaymentProcessorStatusById(final long id) {
		PaymentProcessor paymentProcessor = paymentProcessorDAO
				.findByPaymentProcessorId(id);

		validatePaymentProcessor(paymentProcessor,id);
		
		List<PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
				.findPaymentProccessorRulByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("PaymentProcessorRules size : {}",paymentProcessorRules.size());
		paymentProcessor.setPaymentProcessorRules(paymentProcessorRules);

		List<PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
				.findPaymentProccessorMerchantByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("PaymentProcessorMerchants size : {} ",paymentProcessorMerchants.size());
		paymentProcessor.setPaymentProcessorMerchants(paymentProcessorMerchants);
		ItemStatusResource hasPaymentProcessorName = new ItemStatusResource(1, "Add Payment Processor Name", null,
				true);
		ItemStatusResource hasSameDayProcessing = new ItemStatusResource(2, "Same Day Processing Window", null,
				paymentProcessor.getRemitTransactionCloseTime() != null);
		ItemStatusResource hasMerchantsAssociated = new ItemStatusResource(3, "Add MIDs", null,
				paymentProcessor.hasMerchantsAssociated());
		ItemStatusResource hasRulesAssociated = new ItemStatusResource(4, "Update volume assignment", null,
				paymentProcessor.hasRulesAssociated());

		List<ItemStatusCodeResource> responseCodeItems = paymentProcessorCodeService
				.hasResponseCodesAssociated(paymentProcessor);
		ItemStatusResource hasResponseCodesAssociated = new ItemStatusResource(5, "Add response codes",
				responseCodeItems, hasCodesAssociated(responseCodeItems));

		List<ItemStatusCodeResource> statusCodeItems = paymentProcessorCodeService
				.hasStatusCodesAssociated(paymentProcessor);
		ItemStatusResource hasStatusCodesAssociated = new ItemStatusResource(6, "Add status codes", statusCodeItems,
				hasCodesAssociated(statusCodeItems));

		PaymentProcessorStatusResource paymentProcessorStatusResource = new PaymentProcessorStatusResource();
		paymentProcessorStatusResource.setHasPaymentProcessorName(hasPaymentProcessorName);
		paymentProcessorStatusResource.setHasSameDayProcessing(hasSameDayProcessing);
		paymentProcessorStatusResource.setHasMerchantsAssociated(hasMerchantsAssociated);
		paymentProcessorStatusResource.setHasRulesAssociated(hasRulesAssociated);
		paymentProcessorStatusResource.setHasResponseCodesAssociated(hasResponseCodesAssociated);
		paymentProcessorStatusResource.setHasStatusCodesAssociated(hasStatusCodesAssociated);

		updatePaymentProcessor(paymentProcessor,paymentProcessorStatusResource);

		return paymentProcessorStatusResource;
	}
	
	private void validatePaymentProcessor(PaymentProcessor paymentProcessor,long id){
		if (paymentProcessor == null) {
			throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
		}
	}
	
	private void updatePaymentProcessor(PaymentProcessor paymentProcessor,PaymentProcessorStatusResource paymentProcessorStatusResource){
		if (paymentProcessor.getIsActive() == 1 ) {
			boolean type1 = paymentProcessorStatusResource.getHasPaymentProcessorName().getCompleted()
					&& paymentProcessorStatusResource.getHasSameDayProcessing().getCompleted()
					&& paymentProcessorStatusResource.getHasMerchantsAssociated().getCompleted();
			boolean type2 = type1 && paymentProcessorStatusResource.getHasResponseCodesAssociated().getCompleted()
					&& paymentProcessorStatusResource.getHasRulesAssociated().getCompleted()
					&& paymentProcessorStatusResource.getHasStatusCodesAssociated().getCompleted();
				if ( !(type1 && type2)) {
					paymentProcessor.setIsActive((short) 0);
					LOGGER.debug("Updating paymentProcessor");
					paymentProcessorDAO.update(paymentProcessor);
				}
		}
	}

	private boolean hasCodesAssociated(List<ItemStatusCodeResource> statusCodeItems) {
		LOGGER.debug("statusCodeItems size ={} ",statusCodeItems.size());
		for (ItemStatusCodeResource statusCodeItem : statusCodeItems) {
			if (!statusCodeItem.getCompleted()) {
				return false;
			}
		}
		return true;
	}

}
