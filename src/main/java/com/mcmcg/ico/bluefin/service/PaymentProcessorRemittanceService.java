package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRemittanceRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class PaymentProcessorRemittanceService {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceService.class);

    @Autowired
    private PaymentProcessorRemittanceRepository paymentProcessorRemittanceRepository;
    
    /**
     * This method will find a payment processor remittance by its id, not found exception if it
     * does not exist
     * 
     * @param id
     * @return payment processor remittance object if found
     */
    public PaymentProcessorRemittance getPaymentProcessorRemittanceById(Long id) {
    	PaymentProcessorRemittance paymentProcessorRemittance = paymentProcessorRemittanceRepository.findOne(id);

        if (paymentProcessorRemittance == null) {
            throw new CustomNotFoundException(String.format("Unable to find payment processor remittance with id = [%s]", id));
        }

        return paymentProcessorRemittance;
    }
    
    public List<PaymentProcessorRemittance> getPaymentProcessorRemittances() {
    	return paymentProcessorRemittanceRepository.findAll();
    }
}
