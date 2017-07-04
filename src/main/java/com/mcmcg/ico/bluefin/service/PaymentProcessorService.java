package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant;
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
	public com.mcmcg.ico.bluefin.model.PaymentProcessor getPaymentProcessorById(final long id) {
		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO
				.findByPaymentProcessorId(id);

		if (paymentProcessor == null) {
			throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
		}
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
				.findPaymentProccessorRulByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("PaymentProcessorService :: getPaymentProcessorById() : paymentProcessorRules : "+paymentProcessorRules);
		paymentProcessor.setPaymentProcessorRules(paymentProcessorRules);

		List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
				.findPaymentProccessorMerchantByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("PaymentProcessorService :: getPaymentProcessorById() : paymentProcessorMerchants : "+paymentProcessorMerchants);
		paymentProcessor.setPaymentProcessorMerchants(paymentProcessorMerchants);
		return paymentProcessor;
	}

	/**
	 * This method will return a list of all the payment processors
	 * 
	 * @return List with payment processors that match the criteria given, not
	 *         found exception if not match found
	 */
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessor> getPaymentProcessors() {
		List<com.mcmcg.ico.bluefin.model.PaymentProcessor> result = paymentProcessorDAO.findAll();
		if (result != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("PaymentProcessorService :: getPaymentProcessors() : PaymentProcessor result size : {} ",result.size());
			}
			for (com.mcmcg.ico.bluefin.model.PaymentProcessor processor : result) {
				boolean isReadyToBeActivated = isReadyToBeActivated(processor.getPaymentProcessorId());
				if (processor.isActive() && !isReadyToBeActivated) {
					processor.setIsActive((short) 0);
					isReadyToBeActivated = false;
				}
				processor.setReadyToBeActivated(isReadyToBeActivated);
				List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
						.findPaymentProccessorRulByProcessorId(processor.getPaymentProcessorId());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("PaymentProcessorService :: getPaymentProcessors() : paymentProcessorRules size : {}",paymentProcessorRules.size());
				}
				processor.setPaymentProcessorRules(paymentProcessorRules);
	
				List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
						.findPaymentProccessorMerchantByProcessorId(processor.getPaymentProcessorId());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("PaymentProcessorService :: getPaymentProcessors() : paymentProcessorMerchants size : {} ",paymentProcessorMerchants.size());
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
	public com.mcmcg.ico.bluefin.model.PaymentProcessor createPaymentProcessor(
			BasicPaymentProcessorResource paymentProcessorResource) {
		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorResource.toPaymentProcessor();
		final String processorName = paymentProcessor.getProcessorName();

		LOGGER.debug("PaymentProcessorService :: createPaymentProcessor() : processorName : "+processorName);
		if (existPaymentProcessorName(processorName)) {
			LOGGER.error("Unable to create Payment Processor, this processor already exists: [{}]", processorName);
			throw new CustomBadRequestException(String
					.format("Unable to create Payment Processor, this processor already exists: %s", processorName));
		} else if (paymentProcessorResource.getIsActive() == 1) {
			LOGGER.error("Unable to create Payment Processor, new processor cannot be active: [{}]", processorName);
			throw new CustomBadRequestException(String
					.format("Unable to create Payment Processor, new processor cannot be active: %s", processorName));
		}

		LOGGER.info("PaymentProcessorService :: createPaymentProcessor() : ready to save  paymentProcessor : ");
		return paymentProcessorDAO.save(paymentProcessor);
	}

	/**
	 * Updates the payment processor if it exists by id, if not, an not found
	 * exception will be thrown
	 * 
	 * @param paymentProcessorResource
	 * @return updated PaymentProcessor
	 */
	public com.mcmcg.ico.bluefin.model.PaymentProcessor updatePaymentProcessor(final long id,
			BasicPaymentProcessorResource paymentProcessorResource) {

		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessorToUpdate = getPaymentProcessorById(id);

		LOGGER.debug("PaymentProcessorService :: updatePaymentProcessor() : paymentProcessorToUpdate : "+paymentProcessorToUpdate);
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
		LOGGER.info("PaymentProcessorService :: updatePaymentProcessor() : ready to update  paymentProcessorToUpdate : ");
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
	public com.mcmcg.ico.bluefin.model.PaymentProcessor updatePaymentProcessorMerchants(final long id,
			Set<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource> paymentProcessorMerchants) {

		// Verify if payment processor exists
		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessorToUpdate = getPaymentProcessorById(id);

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
		Map<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource> newMapOfPaymentProcessorMerchants = paymentProcessorMerchants
				.stream().collect(Collectors.toMap(
						com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource::getLegalEntityAppId, p -> p));

		// Temporal list of legal entity app ids already updated
		Set<Long> paymentProcessorMerchantsToKeep = new HashSet<>();
		if (paymentProcessorToUpdate != null) {
			// Update information from current payment processor merchants
			Iterator<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> iter = paymentProcessorToUpdate
					.getPaymentProcessorMerchants().iterator();
			while (iter.hasNext()) {
				com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant element = iter.next();
	
				com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource ppmr = newMapOfPaymentProcessorMerchants
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

	private void addPaymentProcessorMerchants(PaymentProcessor paymentProcessorToUpdate,Set<Long> paymentProcessorMerchantsToKeep,Map<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource> newMapOfPaymentProcessorMerchants){
		for (Entry<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource> legalEntityEntry : newMapOfPaymentProcessorMerchants.entrySet()) {
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
			LOGGER.debug("Exiting from PaymentProcessorService :: updatePaymentProcessorMerchants() ");
		}
	}
	/**
	 * Deletes a payment processor by id, not found exception will be thrown if
	 * payment processor does not exists
	 * 
	 * @param id
	 */
	public void deletePaymentProcessor(final long id) {

		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessorToDelete = getPaymentProcessorById(id);
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
		} else {
			// throw exception
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
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessor> getPaymentProcessorsByIds(Set<Long> paymentProcessorIds) {
		LOGGER.info("Entering to PaymentProcessorService :: getPaymentProcessorsByIds() ");
		List<com.mcmcg.ico.bluefin.model.PaymentProcessor> result = paymentProcessorDAO.findAll(paymentProcessorIds);

		if (result != null && result.size() == paymentProcessorIds.size()) {
			return result;
		}

		// Create a detail error
		if (result == null || result.isEmpty() ) {
			throw new CustomBadRequestException(
					"The following payment processors don't exist.  List = [" + paymentProcessorIds + "]");
		}

		Set<Long> paymentProcessorsNotFound = paymentProcessorIds.stream()
				.filter(x -> !result.stream().map(com.mcmcg.ico.bluefin.model.PaymentProcessor::getPaymentProcessorId)
						.collect(Collectors.toSet()).contains(x))
				.collect(Collectors.toSet());

		throw new CustomBadRequestException(
				"The following payment processors don't exist.  List = [" + paymentProcessorsNotFound + "]");
	}

	private boolean existPaymentProcessorName(String processorName) {
		LOGGER.info("PaymentProcessorService :: existPaymentProcessorName() ");
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
		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO
				.findByPaymentProcessorId(id);

		if (paymentProcessor == null) {
			throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
		}
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
				.findPaymentProccessorRulByProcessorId(paymentProcessor.getPaymentProcessorId());
		LOGGER.debug("PaymentProcessorRules size : {}",paymentProcessorRules.size());
		paymentProcessor.setPaymentProcessorRules(paymentProcessorRules);

		List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
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

		return paymentProcessorStatusResource;
	}

	private boolean hasCodesAssociated(List<ItemStatusCodeResource> statusCodeItems) {
		LOGGER.debug("PaymentProcessorService :: hasCodesAssociated() : statusCodeItems size : "+statusCodeItems.size());
		for (ItemStatusCodeResource statusCodeItem : statusCodeItems) {
			if (!statusCodeItem.getCompleted()) {
				return false;
			}
		}
		return true;
	}

}
