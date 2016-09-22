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

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRemittanceRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.RefundTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class PaymentProcessorRemittanceService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceService.class);
	
    @Autowired
    private SaleTransactionRepository saleTransactionRepository;
    @Autowired
    private VoidTransactionRepository voidTransactionRepository;
    @Autowired
    private RefundTransactionRepository refundTransactionRepository;
    @Autowired
    private PaymentProcessorRemittanceRepository paymentProcessorRemittanceRepository;
    
    public Transaction getTransactionInformation(final String transactionId, TransactionType transactionType, final String processorTransactionType) {
        Transaction result = null;

        switch (transactionType) {
        case VOID:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = voidTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		result = voidTransactionRepository.findByProcessorTransactionId(transactionId);
        	}
            break;
        case REFUND:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = refundTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		result = refundTransactionRepository.findByProcessorTransactionId(transactionId);
        	}
            break;
        case REMITTANCE:
        	// PaymentProcessor_Remittance does not have ApplicationTransactionId
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = null;
        	} else {
        		result = paymentProcessorRemittanceRepository.findByProcessorTransactionID(transactionId);
        	}
            break;
        default:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = saleTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		result = saleTransactionRepository.findByProcessorTransactionId(transactionId);
        	}
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }
    
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
    
    /**
     * Get the value of parameter in search string
     * 
     * @param search string
     * @param parameter in search string
     * 
     * @return value of parameter
     */
    public String getValueFromParameter(String search, String parameter) {
    	
    	String value = "";
    	String[] array1 = search.split("\\$\\$");
    	
    	for (String pair : array1) {
    		if (pair.startsWith(parameter)) {
    			String[] array2 = pair.split(":");
    			value = array2[1];
    			break;
    		}
    	}
    	
        return value;
    }
}
