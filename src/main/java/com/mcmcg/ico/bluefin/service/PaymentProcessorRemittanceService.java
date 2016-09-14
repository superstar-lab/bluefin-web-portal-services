package com.mcmcg.ico.bluefin.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class PaymentProcessorRemittanceService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceService.class);
	
    @Autowired
    private SaleTransactionRepository saleTransactionRepository;
    
    public Iterable<SaleTransaction> getSalesRefundTransactions(String search, PageRequest paging) {
        Page<SaleTransaction> result;
        try {
        	result = saleTransactionRepository.findSalesRefundTransaction(search, paging);
        } catch (ParseException e) {
            throw new CustomNotFoundException("Unable to process find payment processor remittance, due an error with date formatting");
        }
        final int page = paging.getPageNumber();

        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }
    
    public Iterable<PaymentProcessorRemittance> getPaymentProcessorRemittances(String search, PageRequest paging) {
        Page<PaymentProcessorRemittance> result;
        try {
        	result = saleTransactionRepository.findRemittanceTransaction(search, paging);
        } catch (ParseException e) {
            throw new CustomNotFoundException("Unable to process find payment processor remittance, due an error with date formatting");
        }
        final int page = paging.getPageNumber();

        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }
    
    public HashMap<String, String> getReconciliationStatusMap() {
    	return saleTransactionRepository.getReconciliationStatusMap();
    }
    
    @SuppressWarnings("rawtypes")
	public String getKeyFromValue(HashMap<String, String> map, String value) {
    	String key = null;
    	
    	for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
    		Map.Entry e = (Map.Entry) iterator.next();
    		
    		if (e.getValue().toString().equals(value)) {
    			key = e.getKey().toString();
    		}
    	}
    	
    	return key;
    }
}
