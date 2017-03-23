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

import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorMerchantDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRuleDAO;
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
	private PaymentProcessorRepository paymentProcessorRepository;

	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;

	@Autowired
	private PaymentProcessorRuleDAO paymentProcessorRuleDAO;
	
	@Autowired
	private PaymentProcessorMerchantDAO paymentProcessorMerchantDAO;

	@Autowired
	private PaymentProcessorCodeService paymentProcessorCodeService;
	
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
		paymentProcessor.setPaymentProcessorRules(paymentProcessorRules);
		
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
				.findPaymentProccessorMerchantByProcessorId(paymentProcessor.getPaymentProcessorId());
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
		for (com.mcmcg.ico.bluefin.model.PaymentProcessor processor : result) {
			boolean isReadyToBeActivated = isReadyToBeActivated(processor.getPaymentProcessorId());
			if (processor.isActive() && !isReadyToBeActivated) {
				processor.setIsActive((short) 0);
				//paymentProcessorRepository.save(processor);
				isReadyToBeActivated = false;
			}
			processor.setReadyToBeActivated(isReadyToBeActivated);
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = paymentProcessorRuleDAO
					.findPaymentProccessorRulByProcessorId(processor.getPaymentProcessorId());
			processor.setPaymentProcessorRules(paymentProcessorRules);
			
			List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> paymentProcessorMerchants = paymentProcessorMerchantDAO
					.findPaymentProccessorMerchantByProcessorId(processor.getPaymentProcessorId());
			processor.setPaymentProcessorMerchants(paymentProcessorMerchants);
		}

		return result == null ? new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessor>() : result;
	}

	/**
	 * This method will create a new Payment Processor that does not exist
	 * already, if name exists will throw a bad request exception
	 * 
	 * @param paymentProcessorResource
	 * @return Payment processor created
	 */
	public com.mcmcg.ico.bluefin.model.PaymentProcessor createPaymentProcessor(BasicPaymentProcessorResource paymentProcessorResource) {
		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorResource.toPaymentProcessor();
		final String processorName = paymentProcessor.getProcessorName();

		if (existPaymentProcessorName(processorName)) {
			LOGGER.error("Unable to create Payment Processor, this processor already exists: [{}]", processorName);
			throw new CustomBadRequestException(String
					.format("Unable to create Payment Processor, this processor already exists: %s", processorName));
		} else if (paymentProcessorResource.getIsActive() == 1) {
			LOGGER.error("Unable to create Payment Processor, new processor cannot be active: [{}]", processorName);
			throw new CustomBadRequestException(String
					.format("Unable to create Payment Processor, new processor cannot be active: %s", processorName));
		}

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

		if (paymentProcessorToUpdate.getIsActive() == 0 && !isReadyToBeActivated(id)) {
			paymentProcessorToUpdate.setIsActive((short) 0);
			paymentProcessorDAO.save(paymentProcessorToUpdate);
		}
		if (paymentProcessorResource.getIsActive() == 1 && !isReadyToBeActivated(id)) {
			LOGGER.error("Unable to activate Payment Processor, processor has some pending steps: [{}]",
					paymentProcessorToUpdate.getProcessorName());
			throw new CustomBadRequestException(
					String.format("Unable to activate Payment Processor, processor has some pending steps: %s",
							paymentProcessorToUpdate.getProcessorName()));
		}

		// Update fields for existing Payment Processor
		paymentProcessorToUpdate.setProcessorName(paymentProcessorResource.getProcessorName());
		paymentProcessorToUpdate.setRemitTransactionOpenTime(paymentProcessorResource.getRemitTransactionOpenTime());
		paymentProcessorToUpdate.setRemitTransactionCloseTime(paymentProcessorResource.getRemitTransactionCloseTime());
		paymentProcessorToUpdate.setIsActive(paymentProcessorResource.getIsActive());

		return paymentProcessorToUpdate;
	}

	public boolean isReadyToBeActivated(final long id) {
		PaymentProcessorStatusResource paymentProcessorStatus = getPaymentProcessorStatusById(id);
		return paymentProcessorStatus.getHasPaymentProcessorName().getCompleted()
				&& paymentProcessorStatus.getHasSameDayProcessing().getCompleted()
				&& paymentProcessorStatus.getHasMerchantsAssociated().getCompleted()
				&& paymentProcessorStatus.getHasResponseCodesAssociated().getCompleted()
				&& paymentProcessorStatus.getHasRulesAssociated().getCompleted()
				&& paymentProcessorStatus.getHasStatusCodesAssociated().getCompleted();
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

        // User wants to clear payment processor merchants from payment
        // processor
        if (paymentProcessorMerchants.isEmpty()) {
            paymentProcessorToUpdate.getPaymentProcessorMerchants().clear();
            return paymentProcessorDAO.update(paymentProcessorToUpdate);
        }

        // New payment processor merchants that need to be created or updated
        Map<Long, com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource> newMapOfPaymentProcessorMerchants = paymentProcessorMerchants
                .stream().collect(Collectors.toMap(com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource::getLegalEntityAppId, p -> p));

        // Temporal list of legal entity app ids already updated
        Set<Long> PaymentProcessorMerchantsToKeep = new HashSet<Long>();

        // Update information from current payment processor merchants
        Iterator<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> iter = paymentProcessorToUpdate.getPaymentProcessorMerchants().iterator();
        while (iter.hasNext()) {
        	com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant element = iter.next();

        	com.mcmcg.ico.bluefin.model.PaymentProcessorMerchantResource ppmr = newMapOfPaymentProcessorMerchants
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

        return paymentProcessorDAO.save(paymentProcessorToUpdate);
    
	}

	/**
	 * Deletes a payment processor by id, not found exception will be thrown if
	 * payment processor does not exists
	 * 
	 * @param id
	 */
	public void deletePaymentProcessor(final long id) {

		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessorToDelete = getPaymentProcessorById(id);
		if (paymentProcessorToDelete.getPaymentProcessorRules() != null && !paymentProcessorToDelete.getPaymentProcessorRules().isEmpty()) {
			paymentProcessorRuleDAO.deletePaymentProcessorRules(paymentProcessorToDelete.getPaymentProcessorId());
		}
		if (paymentProcessorToDelete.getPaymentProcessorMerchants() != null && !paymentProcessorToDelete.getPaymentProcessorMerchants().isEmpty()) {
			paymentProcessorMerchantDAO.deletePaymentProcessorRules(paymentProcessorToDelete.getPaymentProcessorId());	
		}
		/* TODO 1. - Before deleting Status Code Need to delete PaymentProcessorInternalStatusCode 
		paymentProcessorCodeService.deletePaymentProcessorStatusCode(paymentProcessorToDelete.getPaymentProcessorId());
		 * TODO 2. - Need to Delete PaymentProcessorResponseCode and it is also dependent on PaymentProcessorInternalRespnseCode.*/
		paymentProcessorDAO.delete(paymentProcessorToDelete);
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
		List<com.mcmcg.ico.bluefin.model.PaymentProcessor> result = paymentProcessorDAO.findAll(paymentProcessorIds);

		if (result.size() == paymentProcessorIds.size()) {
			return result;
		}

		// Create a detail error
		if (result == null || result.isEmpty()) {
			throw new CustomBadRequestException(
					"The following payment processors don't exist.  List = [" + paymentProcessorIds + "]");
		}

		Set<Long> paymentProcessorsNotFound = paymentProcessorIds.stream().filter(x -> !result.stream()
				.map(com.mcmcg.ico.bluefin.model.PaymentProcessor::getPaymentProcessorId).collect(Collectors.toSet()).contains(x))
				.collect(Collectors.toSet());

		throw new CustomBadRequestException(
				"The following payment processors don't exist.  List = [" + paymentProcessorsNotFound + "]");
	}

	private boolean existPaymentProcessorName(String processorName) {
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
		com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(id);

		if (paymentProcessor == null) {
			throw new CustomNotFoundException(String.format("Unable to find payment processor with id = [%s]", id));
		}
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

		if (paymentProcessor.getIsActive() == 1
				&& !(paymentProcessorStatusResource.getHasPaymentProcessorName().getCompleted()
						&& paymentProcessorStatusResource.getHasSameDayProcessing().getCompleted()
						&& paymentProcessorStatusResource.getHasMerchantsAssociated().getCompleted()
						&& paymentProcessorStatusResource.getHasResponseCodesAssociated().getCompleted()
						&& paymentProcessorStatusResource.getHasRulesAssociated().getCompleted()
						&& paymentProcessorStatusResource.getHasStatusCodesAssociated().getCompleted())) {
			paymentProcessor.setIsActive((short) 0);
			//paymentProcessorRepository.save(paymentProcessor);
		}

		return paymentProcessorStatusResource;
	}

	private boolean hasCodesAssociated(List<ItemStatusCodeResource> statusCodeItems) {
		for (ItemStatusCodeResource statusCodeItem : statusCodeItems) {
			if (!statusCodeItem.getCompleted()) {
				return false;
			}
		}
		return true;
	}

}
